package focus.search.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusParser;
import focus.search.bnf.ModelBuild;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.controller.common.Base;
import focus.search.controller.common.FormulaAnalysis;
import focus.search.controller.common.FormulaCase;
import focus.search.meta.AmbiguitiesRecord;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Formula;
import focus.search.metaReceived.*;
import focus.search.response.exception.*;
import focus.search.response.search.*;
import focus.search.suggestions.HistoryUtils;
import focus.search.suggestions.SourcesUtils;
import focus.search.suggestions.SuggestionUtils;
import org.apache.log4j.Logger;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * creator: sunc
 * date: 2018/1/31
 * description:
 */
class SearchHandler {
    private static final Logger logger = Logger.getLogger(SearchHandler.class);

    private static final List<String> array = Arrays.asList("putFormula", "modifyFormula", "deleteFormula");
    private static final List<String> noImmediateResponse = Arrays.asList("init", "echo", "exportContext", "formulaCase");

    static void preHandle(WebSocketSession session, JSONObject params) throws IOException, FocusHttpException, FocusInstructionException, FocusParserException, IllegalException {
        logger.info("lastParams: " + params);
        Object object = session.getAttributes().get("user");
        JSONObject user = object == null ? new JSONObject() : (JSONObject) object;
        String type = params.getString("type");
        String category = user.getString("category");
        JSONObject datas = new JSONObject();
        JSONArray formulas = new JSONArray();
        if (type.equalsIgnoreCase("category")) {
            category = params.getString("category");
        } else if (array.contains(type)) {
            formulas = params.getJSONArray("datas");
        } else {
            datas = params.getJSONObject("datas");
        }

        if (!noImmediateResponse.contains(type))
            // response immediately
            Common.send(session, Response.response(type));

        switch (type) {
            case "init":
                init(session, datas, user);
                break;
            case "search":
                search(session, datas, user);
                break;
            case "selectSuggest":// 使用场景未知
                selectSuggest(session, datas);
                break;
            case "disambiguite":
                disambiguite(session, datas, user);
                break;
            case "reDisambiguite":
                reDisambiguite(session, datas, user);
                break;
            case "clearDisambiguite":
                clearDisambiguite(session, datas, user);
                break;
            case "axis":// 使用场景未知
                axis(session, datas);
                break;
            case "test":// 可废弃
                test(session, datas);
                break;
            case "formula":
                formula(session, datas, user);//  公式内容搜索
                break;
            case "fnamecheck":
                fnamecheck(session, datas, user);
                break;
            case "lang":// 可废弃
                lang(session, datas, user);
                break;
            case "category":// 可废弃
                category(session, category, user);
                break;
            case "exportContext":
                exportContext(session, user);// 保存 answer 时需先导出 context
                break;
            case "importContext":// 可废弃
                importContext(session, datas);
                break;
            case "putFormula":
                putFormula(session, formulas, user);
                break;
            case "modifyFormula":
                modifyFormula(session, formulas, user);
                break;
            case "deleteFormula":
                deleteFormula(session, formulas, user);
                break;
            case "formulaCase":
                formulaCase(session, datas, user);
                break;
            case "echo":
            default:
                echo(session);
        }
    }

    // category :
    // - question : search
    // - expressionOrLogicalExpression : formula
    private static void init(WebSocketSession session, JSONObject datas, JSONObject user) throws IOException, FocusHttpException {
        String category = datas.getString("category");
        String context = datas.getString("context");
        String curSearchToken = datas.getString("curSearchToken");
        String language = datas.getString("lang");
        String sourceToken = datas.getString("sourceToken");

        if (Common.isEmpty(sourceToken)) {
            Common.send(session, ErrorResponse.response(Constant.ErrorType.NULL_SOURCETOKEN).toJSONString());
            return;
        }

        user.put("category", category);
        user.put("language", language);
        user.put("sourceToken", sourceToken);
        session.getAttributes().put("sourceToken", sourceToken);
        user.put("curSearchToken", curSearchToken);
        JSONObject getSource;
        getSource = Clients.WebServer.getSource(sourceToken);
        logger.info(getSource.toJSONString());

        InitResponse response = new InitResponse("response", "init");
        response.setSourceToken(sourceToken);
        InitResponse.Init init = new InitResponse.Init();
        if (!"success".equals(getSource.getString("status"))) {
            init.setStatus("fail");
            init.setMessage("get sources from webserver fail.");
        } else {
            init.setStatus("success");
            init.setMessage("get sources from webserver success.");
            List<SourceReceived> srs = JSONArray.parseArray(getSource.getJSONArray("sources").toJSONString(), SourceReceived.class);
            List<RelationReceived> rrs = JSONArray.parseArray(JSONArray.toJSONString(getSource.getJSONArray("sources"),
                    new Common.JSONFilter()), RelationReceived.class);

            user.put("sources", srs);
            JSONArray sourceList = new JSONArray();
            srs.forEach(sr -> sourceList.add(sr.tableId));
            user.put("sourceList", sourceList.toJSONString());

            // 初始化历史问题
            user.put("historyQuestions", HistoryUtils.initHistory(user));

            // 歧义记录
            JSONObject ambiguities = new JSONObject();

            // 根据 context 恢复歧义|上下文|语言等
            if (context != null) {
                JSONObject contextJson = JSONObject.parseObject(context);
                ambiguities = Base.context(contextJson, srs);

                // 恢复语言环境
                language = contextJson.getString("language");
                user.put("language", language);

                //  恢复公式
                String formulaStr = contextJson.getString("formulas");
                if (!Common.isEmpty(formulaStr)) {
                    List<Formula> formulas = JSONArray.parseArray(formulaStr, Formula.class);
                    init.setFormulas(formulas);
                    user.put("formulas", formulas);
                }

            }
            user.put("ambiguities", ambiguities);

            for (SourceReceived sr : srs) {
                init.addSource(sr.transfer());
            }

            FocusParser fp = Constant.Language.ENGLISH.equals(language) ? Base.englishParser.deepClone() : Base.chineseParser.deepClone();
            ModelBuild.buildTable(fp, srs);
            user.put("parser", fp);
        }
        response.setDatas(init.toJson());
        logger.info(response.response());
        logger.info("after init user: " + user);
        Common.send(session, response.response());

    }

    private static void search(WebSocketSession session, JSONObject params, JSONObject user) throws IOException, FocusHttpException, FocusInstructionException, FocusParserException, IllegalException {
        logger.info("lastParams:" + params);
        String search = params.getString("search");
        String event = params.getString("event");
        int position = params.getInteger("position");
        JSONObject biConfig = params.getJSONObject("biConfig");
        boolean debug = params.getBoolean("debug");
        List<String> queryFlags = JSONArray.parseArray(params.getString("queryFlags"), String.class);
        List<Ambiguities> ambiguities = JSONArray.parseArray(params.getString("ambiguities"), Ambiguities.class);

        user.put("category", Constant.CategoryType.QUESTION);
//        session.getAttributes().put("user", user);

        Base.response(session, search, user, ambiguities, event, position, biConfig);
    }

    private static void selectSuggest(WebSocketSession session, JSONObject params) {
    }

    private static void disambiguite(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        logger.info("lastParams:" + params);
        String id = params.getString("id");
        int index = params.getInteger("index");
        JSONObject amb = user.getJSONObject("ambiguities");
        logger.info("current all ambiguities:" + amb);
        AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getById(id, amb);
        if (ambiguitiesResolve == null) {
            FocusExceptionHandler.handle(session, ErrorResponse.response(Constant.ErrorType.AMBIGUITY_EXPIRED).toJSONString());
            return;
        }
        logger.info("current ambiguities:" + ambiguitiesResolve.toJSON());
        if (index >= ambiguitiesResolve.ars.size()) {
            FocusExceptionHandler.handle(session, ErrorResponse.response(Constant.ErrorType.AMBIGUITY_OUT_OF_INDEX).toJSONString());
            return;
        }
        AmbiguitiesRecord ar = ambiguitiesResolve.ars.remove(index);
        ambiguitiesResolve.ars.add(0, ar);
        ambiguitiesResolve.isResolved = true;
        amb.put(id, ambiguitiesResolve);
        user.put("ambiguities", amb);
//        session.getAttributes().put("user", user);
        JSONObject response = new JSONObject();
        response.put("type", "state");
        response.put("message", "disambiguiteDone");
        Common.send(session, response.toJSONString());

    }

    private static void reDisambiguite(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        disambiguite(session, params, user);
    }

    private static void clearDisambiguite(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        user.put("ambiguities", new JSONObject());
//        session.getAttributes().put("user", user);
        JSONObject response = new JSONObject();
        response.put("type", "state");
        response.put("message", "clearDisambiguiteDone");
        Common.send(session, response.toJSONString());
    }

    private static void axis(WebSocketSession session, JSONObject params) {

    }

    private static void test(WebSocketSession session, JSONObject params) {

    }

    private static void formula(WebSocketSession session, JSONObject params, JSONObject user) throws IOException, FocusHttpException, FocusInstructionException, FocusParserException, IllegalException {
        String search = params.getString("formula");
        int position = params.getInteger("position");
        Boolean debug = params.getBoolean("debug");

        user.put("category", Constant.CategoryType.EXPRESSION);

        Base.response(session, search, user, position);

    }

    private static void fnamecheck(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        String name = params.getString("name");
        String id = params.getString("id");
        JSONObject response = new JSONObject();
        response.put("type", "fnamecheck");
        JSONObject datas = new JSONObject();
        String message = fnamecheck(name, id, user);
        datas.put("message", message == null ? "success" : message);
        response.put("datas", datas);
        Common.send(session, response.toJSONString());
    }

    private static void lang(WebSocketSession session, JSONObject params, JSONObject user) {

    }

    private static void category(WebSocketSession session, String category, JSONObject user) {
        user.put("category", category);
    }

    private static void exportContext(WebSocketSession session, JSONObject user) throws IOException {
        // 歧义
        JSONObject context = new JSONObject();
        JSONObject amb = user.getJSONObject("ambiguities");
        JSONArray disambiguitions = new JSONArray();
        if (amb != null)
            for (Object obj : amb.values()) {
                AmbiguitiesResolve tmp = (AmbiguitiesResolve) obj;
                if (tmp.isResolved) {
                    AmbiguitiesRecord ar = tmp.ars.get(0);
                    disambiguitions.add(ar.toJSON());
                }
            }
        context.put("disambiguitions", disambiguitions.isEmpty() ? null : disambiguitions);
        context.put("indexs", null);
        context.put("language", user.getString("language"));
        List<Formula> formulas = Base.getFormula(user);
        context.put("formulas", formulas.isEmpty() ? null : formulas);
        Common.send(session, ExportContextResponse.response(context));

    }

    private static void importContext(WebSocketSession session, JSONObject params) {

    }

    private static void putFormula(WebSocketSession session, JSONArray params, JSONObject user) throws IOException, FocusParserException, FocusInstructionException, IllegalException {
        FocusParser fp = (FocusParser) user.get("parser");
        String language = user.getString("language");
        JSONObject amb = user.getJSONObject("ambiguities");
        List<FormulaReceived> formulaReceivedList = JSONArray.parseArray(params.toJSONString(), FormulaReceived.class);

        FormulaControllerResponse response = new FormulaControllerResponse();
        List<Formula> formulas = Base.getFormula(user);

        List<Formula> formulaRules = new ArrayList<>();
        for (FormulaReceived formulaReceived : formulaReceivedList) {
            JSONObject json = new JSONObject();

            //  检测重名
            if (isFormulaNameExist(formulaReceived.name, null, user)) {
                json.put("status", "haveExisted");
                json.put("message", "haveExisted");
                response.datas.add(json);
                continue;
            }
            json.put("formulaObj", null);
            json.put("message", null);
            Formula formula = new Formula();
            List<FocusToken> tokens = null;
            try {
                // TODO: 2018/5/21 ambiguity
                tokens = fp.focusAnalyzer.test(formulaReceived.formula, language);
            } catch (AmbiguitiesException e) {
                e.printStackTrace();
            }
            try {
                FocusInst focusInst = fp.parseFormula(tokens, amb, user.getInteger("id"));
                FormulaAnalysis.FormulaObj formulaObj = FormulaAnalysis.analysis(focusInst.lastFocusPhrase());

                formula.setColumnType(formulaReceived.columnType);
                formula.setAggregation(formulaReceived.aggregation);
                formula.setDataType(formulaReceived.dataType);
                formula.setName(formulaReceived.name);
                formula.setFormula(formulaReceived.formula);
                formula.setId(UUID.randomUUID().toString());
                formula.setInstruction(formulaObj.toJSON());

                formulas.add(formula);
                formulaRules.add(formula);

                json.put("formulaObj", formula.toJSON());
                json.put("message", "done");
                json.put("status", "success");
            } catch (AmbiguitiesException e) {
                logger.error(e.getMessage());

                json.put("status", "illegal");

            } catch (IllegalException e) {
                e.question = formulaReceived.formula;
                throw e;
            }

            response.datas.add(json);
        }
        Common.send(session, response.response("putFormula"));
        user.put("formulas", formulas);

        // 公式名添加到分词和规则中
        if (formulaRules.size() > 0) {
            ModelBuild.buildFormulas(fp, formulaRules);
        }
    }

    private static void modifyFormula(WebSocketSession session, JSONArray params, JSONObject user) throws IOException, IllegalException {
        FocusParser fp = (FocusParser) user.get("parser");
        String language = user.getString("language");
        JSONObject amb = user.getJSONObject("ambiguities");
        List<Formula> formulas = Base.getFormula(user);

        FormulaControllerResponse response = new FormulaControllerResponse();
        List<FormulaReceived> formulaReceivedList = JSONArray.parseArray(params.toJSONString(), FormulaReceived.class);
        for (FormulaReceived formulaReceived : formulaReceivedList) {
            JSONObject json = new JSONObject();
            //  检测重名
            if (isFormulaNameExist(formulaReceived.name, formulaReceived.id, user)) {
                json.put("status", "haveExisted");
                json.put("message", "haveExisted");
                response.datas.add(json);
                continue;
            }
            for (Formula formula : formulas) {
                if (formulaReceived.id.equalsIgnoreCase(formula.getId())) {

                    formulas.remove(formula);

                    json.put("formulaObj", null);
                    json.put("message", null);

                    List<FocusToken> tokens = null;
                    try {
                        // TODO: 2018/5/21 ambiguity
                        tokens = fp.focusAnalyzer.test(formulaReceived.formula, language);
                    } catch (AmbiguitiesException e) {
                        e.printStackTrace();
                    }
                    try {
                        FocusInst focusInst = fp.parseFormula(tokens, amb, user.getInteger("id"));
                        FormulaAnalysis.FormulaObj formulaObj = FormulaAnalysis.analysis(focusInst.lastFocusPhrase());

                        formula.setColumnType(formulaReceived.columnType);
                        formula.setAggregation(formulaReceived.aggregation);
                        formula.setDataType(formulaReceived.dataType);
                        formula.setName(formulaReceived.name);
                        formula.setFormula(formulaReceived.formula);
                        formula.setInstruction(formulaObj.toJSON());

                        formulas.add(formula);

                        json.put("formulaObj", formula.toJSON());
                        json.put("message", "done");
                        json.put("status", "success");
                    } catch (FocusInstructionException | AmbiguitiesException e) {
                        logger.error(e.getMessage());
                        json.put("status", "illegal");
                    } catch (IllegalException e) {
                        e.question = formulaReceived.formula;
                        throw e;
                    }
                    response.datas.add(json);
                    break;
                }
            }
        }
        Common.send(session, response.response("modifyFormula"));
    }

    private static void deleteFormula(WebSocketSession session, JSONArray params, JSONObject user) throws IOException {
        List<Formula> formulas = Base.getFormula(user);
        FormulaControllerResponse response = new FormulaControllerResponse();
        List<String> formulaNames = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            JSONObject json = params.getJSONObject(i);
            for (Formula formula : formulas) {
                if (formula.getId().equalsIgnoreCase(json.getString("id"))) {
                    formulas.remove(formula);
                    formulaNames.add(formula.getName());
                    json.put("status", "success");
                    json.put("message", "done");
                    response.datas.add(json);
                    break;
                }
            }
        }
        user.put("formulas", formulas);
        Common.send(session, response.response("deleteFormula"));
        FocusParser fp = (FocusParser) user.get("parser");
        ModelBuild.deleteFormulas(fp, formulaNames);
    }

    private static void formulaCase(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        String keyword = params.getString("keyword");
        FormulaCaseResponse response = new FormulaCaseResponse();
        //        {"keyword":"+", "cases":["4 + 1", "47.69 + 99.34", "userid + userid"]}
        JSONObject datas = new JSONObject();
        datas.put("keyword", keyword);
        JSONArray jsonArray = FormulaCase.buildCase(user, keyword);
        JSONArray cases = new JSONArray();
        if (jsonArray == null || jsonArray.size() <= 2) {
            cases = jsonArray;
        } else {
            while (cases.size() < 2) {
                String value = jsonArray.get(SourcesUtils.decimalSug(jsonArray.size())).toString();
                if (!cases.contains(value)) {
                    cases.add(value);
                }
            }
        }
        datas.put("cases", cases);
        response.setDatas(datas);
        Common.send(session, response.response());
    }

    private static void echo(WebSocketSession session) throws IOException {
        JSONObject response = new JSONObject();
        response.put("type", "echo");
        Common.send(session, response.toJSONString());
    }

    // textChange
    private static void textChange() {

    }

    // focusIn
    private static void focusIn(WebSocketSession session, String question, int position, FocusParser fp) throws IOException {
    }

    // move
    private static void move() {

    }

    private static String fnamecheck(String name, String id, JSONObject user) {
        FocusParser fp = (FocusParser) user.get("parser");
        // 系统关键词
        // 1. 符号
        List<TerminalToken> tokens = SuggestionUtils.terminalTokens(fp, "<symbol>");
        for (TerminalToken token : tokens) {
            if (name.toLowerCase().contains(token.getName())) {
                return "conflictWithKeyword";
            }
        }
        // 2. 其他关键词
        for (TerminalToken token : fp.getTerminalTokens()) {
            if (name.toLowerCase().equals(token.getName().toLowerCase())) {
                return "conflictWithKeyword";
            }
        }

        //  纯数字
        if (Common.intCheck(name) || Common.doubleCheck(name)) {
            return "illegalLetter";
        }

        //  表名列名
        List<SourceReceived> srs = JSONArray.parseArray(user.getJSONArray("sources").toJSONString(), SourceReceived.class);
        for (SourceReceived source : srs) {
            if (source.sourceName.equalsIgnoreCase(name)) {
                return "conflictWithKeyword";
            }
            for (ColumnReceived column : source.columns) {
                if (column.columnDisplayName.equalsIgnoreCase(name)) {
                    return "conflictWithColumnName";
                }
            }
        }
        if (isFormulaNameExist(name, id, user)) {
            return "haveExisted";
        }
        //todo  其他非法字符
        return null;

    }

    private static boolean isFormulaNameExist(String name, String id, JSONObject user) {
        List<Formula> formulas = Base.getFormula(user);
        boolean isIdNull = Common.isEmpty(id);
        for (Formula formula : formulas) {
            if (formula.getName().equalsIgnoreCase(name)) {
                if (isIdNull) {
                    return true;
                } else if (!formula.getId().equalsIgnoreCase(id)) {
                    return true;
                }
            }
        }
        return false;
    }

}
