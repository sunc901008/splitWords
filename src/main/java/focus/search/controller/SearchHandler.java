package focus.search.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.base.LoggerHandler;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.controller.common.FormulaAnalysis;
import focus.search.controller.common.FormulaCase;
import focus.search.controller.common.SuggestionBuild;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.InstructionBuild;
import focus.search.meta.AmbiguitiesRecord;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.metaReceived.*;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.search.*;
import org.springframework.web.socket.TextMessage;
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
    private static final List<String> array = Arrays.asList("putFormula", "modifyFormula", "deleteFormula");

    static void preHandle(WebSocketSession session, JSONObject params) throws IOException {
        LoggerHandler.info("params: " + params);
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

        if (!type.equals("exportContext"))
            // response immediately
            session.sendMessage(new TextMessage(Response.response(type)));

        switch (type) {
            case "init":
                init(session, datas, user);
                break;
            case "search":
                search(session, datas, user);// todo event
                break;
            case "selectSuggest":// todo 使用场景未知
                selectSuggest(session, datas);
                break;
            case "disambiguate":
                disambiguate(session, datas, user);
                break;
            case "reDisambiguate":
                reDisambiguate(session, datas, user);
                break;
            case "clearDisambiguate":
                clearDisambiguate(session, datas, user);
                break;
            case "axis":// todo 使用场景未知
                axis(session, datas);
                break;
            case "test":// todo 可废弃
                test(session, datas);
                break;
            case "formula":
                formula(session, datas, user);//  公式内容搜索
                break;
            case "fnamecheck":
                fnamecheck(session, datas, user);
                break;
            case "lang":// todo 可废弃
                lang(session, datas, user);
                break;
            case "category":// todo 可废弃
                category(session, category, user);
                break;
            case "exportContext":
                exportContext(session, user);// 保存 answer 时需先导出 context
                break;
            case "importContext":// todo 可废弃
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
                echo(session, user);
        }
    }

    // category :
    // - question : search
    // - expressionOrLogicalExpression : formula
    private static void init(WebSocketSession session, JSONObject datas, JSONObject user) throws IOException {
        String category = datas.getString("category");
        String context = datas.getString("context");
        String curSearchToken = datas.getString("curSearchToken");
        String language = datas.getString("lang");
        String sourceToken = datas.getString("sourceToken");

        user.put("category", category);
        user.put("language", language);
        user.put("sourceToken", sourceToken);
        user.put("curSearchToken", curSearchToken);
        JSONObject getSource;
        try {
            getSource = Clients.WebServer.getSource(sourceToken);
            //todo will delete next line
            session.sendMessage(new TextMessage(getSource.toJSONString()));
        } catch (Exception e) {
            // todo exception controller
            return;
        }

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

            // 歧义记录
            JSONObject ambiguities = new JSONObject();

            // 根据 context 恢复歧义|上下文|语言等
            if (context != null) {
                JSONObject contextJson = JSONObject.parseObject(context);
                String contextStr = contextJson.getString("disambiguations");// 检测 lisp 返回的空值 nil
                if (!Common.isEmpty(contextStr) && !contextStr.equalsIgnoreCase("NIL")) {

                    // 恢复歧义
                    JSONArray disambiguations = JSONArray.parseArray(contextStr);

                    for (Object obj : disambiguations) {
                        JSONObject col = JSONObject.parseObject(obj.toString());
                        String columnName = col.getString("columnName");
                        int columnId = col.getInteger("columnId");
                        AmbiguitiesResolve ar = new AmbiguitiesResolve();
                        ar.value = columnName;
                        ar.isResolved = true;

                        List<Column> columns = CommonFunc.getColumns(columnName, srs);
                        for (Column column : columns) {
                            AmbiguitiesRecord ambiguitiesRecord = new AmbiguitiesRecord();
                            ambiguitiesRecord.sourceName = column.getSourceName();
                            ambiguitiesRecord.columnName = columnName;
                            ambiguitiesRecord.columnId = column.getColumnId();
                            ambiguitiesRecord.type = Constant.FNDType.COLUMN;
                            ar.ars.add(ambiguitiesRecord);
                        }
                        for (AmbiguitiesRecord a : ar.ars) {
                            if (a.columnId == columnId) {
                                ar.ars.remove(a);
                                ar.ars.add(0, a);
                                break;
                            }
                        }

                        ambiguities.put(UUID.randomUUID().toString(), ar);
                    }
                }

                // 恢复语言环境
                user.put("language", contextJson.getString("language"));

                //  恢复公式
                String formulaStr = contextJson.getString("formulas");
                if (!Common.isEmpty(formulaStr)) {
                    List<Formula> formulas = JSONArray.parseArray(formulaStr, Formula.class);
                    init.setFormulas(formulas);
                    user.put("formulas", formulas);
                }

            }
            user.put("ambiguities", ambiguities);

            System.out.println(JSON.toJSONString(srs));

            //todo will delete next line
            session.sendMessage(new TextMessage(JSON.toJSONString(rrs)));
            for (SourceReceived sr : srs) {
                init.addSource(sr.transfer());
            }

            FocusParser fp = new FocusParser();
            ModelBuild.buildTable(fp, srs);
            user.put("parser", fp);
        }
        response.setDatas(init.toJson());
        LoggerHandler.info(response.response(), Constant.PRINT_LOG);
        session.sendMessage(new TextMessage(response.response()));

    }

    private static void search(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        String search = params.getString("search");
        String event = params.getString("event");
        int position = params.getInteger("position");
        JSONObject biConfig = params.getJSONObject("biConfig");
        boolean debug = params.getBoolean("debug");
        List<String> queryFlags = JSONArray.parseArray(params.getString("queryFlags"), String.class);
        List<Ambiguities> ambiguities = JSONArray.parseArray(params.getString("ambiguities"), Ambiguities.class);

        if (Constant.Event.FOCUS_IN.equalsIgnoreCase(event)) {
            return;
        }

        user.put("category", Constant.CategoryType.QUESTION);
//        session.getAttributes().put("user", user);

        response(session, search, user);

    }

    private static void selectSuggest(WebSocketSession session, JSONObject params) throws IOException {
    }

    private static void disambiguate(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        String id = params.getString("id");
        int index = params.getInteger("index");
        JSONObject amb = user.getJSONObject("ambiguities");
        AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getById(id, amb);
        AmbiguitiesRecord ar = ambiguitiesResolve.ars.remove(index);
        ambiguitiesResolve.ars.add(0, ar);
        ambiguitiesResolve.isResolved = true;
        amb.put(id, ambiguitiesResolve);
        user.put("ambiguities", amb);
//        session.getAttributes().put("user", user);
        JSONObject response = new JSONObject();
        response.put("type", "state");
        response.put("message", "disambiguateDone");
        session.sendMessage(new TextMessage(response.toJSONString()));

    }

    private static void reDisambiguate(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        disambiguate(session, params, user);
    }

    private static void clearDisambiguate(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        user.put("ambiguities", new JSONObject());
//        session.getAttributes().put("user", user);
        JSONObject response = new JSONObject();
        response.put("type", "state");
        response.put("message", "clearDisambiguateDone");
        session.sendMessage(new TextMessage(response.toJSONString()));
    }

    private static void axis(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void test(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void formula(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        String search = params.getString("formula");
        int position = params.getInteger("position");
        boolean debug = params.getBoolean("debug");

        user.put("category", Constant.CategoryType.EXPRESSION);
//        session.getAttributes().put("user", user);

        response(session, search, user);

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
        session.sendMessage(new TextMessage(response.toJSONString()));
    }

    private static void lang(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {

    }

    private static void category(WebSocketSession session, String category, JSONObject user) throws IOException {
        user.put("category", category);
//        session.getAttributes().put("user", user);
    }

    private static void exportContext(WebSocketSession session, JSONObject user) throws IOException {
        // 歧义
        JSONObject context = new JSONObject();
        JSONObject amb = user.getJSONObject("ambiguities");
        JSONArray disambiguations = new JSONArray();
        if (amb != null)
            for (Object obj : amb.values()) {
                AmbiguitiesResolve tmp = (AmbiguitiesResolve) obj;
                if (tmp.isResolved) {
                    JSONObject disambiguation = new JSONObject();
                    AmbiguitiesRecord ar = tmp.ars.get(0);
                    disambiguation.put("columnName", ar.columnName);
                    disambiguation.put("columnId", ar.columnId);
                    disambiguations.add(disambiguation);
                }
            }
        context.put("disambiguations", disambiguations.isEmpty() ? null : disambiguations);
        context.put("indexs", null);
        context.put("language", user.getString("language"));
        List<Formula> formulas = getFormula(user);
        context.put("formulas", formulas.isEmpty() ? null : formulas);
        session.sendMessage(new TextMessage(ExportContextResponse.response(context)));

    }

    private static void importContext(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void putFormula(WebSocketSession session, JSONArray params, JSONObject user) throws IOException {
        FocusParser fp = (FocusParser) user.get("parser");
        String language = user.getString("language");
        JSONObject amb = user.getJSONObject("ambiguities");
        List<FormulaReceived> formulaReceivedList = JSONArray.parseArray(params.toJSONString(), FormulaReceived.class);

        FormulaControllerResponse response = new FormulaControllerResponse();
        List<Formula> formulas = getFormula(user);

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
            List<FocusToken> tokens = fp.focusAnalyzer.test(formulaReceived.formula, language);
            try {
                FocusInst focusInst = fp.parseFormula(tokens, amb);
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
            } catch (InvalidRuleException | AmbiguitiesException e) {
                LoggerHandler.info(e.getMessage());

                json.put("status", "illegal");

            }
            response.datas.add(json);
        }
        session.sendMessage(new TextMessage(response.response("putFormula")));
        user.put("formulas", formulas);

        // 公式名添加到分词和规则中
        if (formulaRules.size() > 0) {
            ModelBuild.buildFormulas(fp, formulaRules);
        }
//        session.getAttributes().put("user", user);
    }

    private static void modifyFormula(WebSocketSession session, JSONArray params, JSONObject user) throws IOException {
        FocusParser fp = (FocusParser) user.get("parser");
        String language = user.getString("language");
        JSONObject amb = user.getJSONObject("ambiguities");
        List<Formula> formulas = getFormula(user);

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

                    List<FocusToken> tokens = fp.focusAnalyzer.test(formulaReceived.formula, language);
                    try {
                        FocusInst focusInst = fp.parseFormula(tokens, amb);
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
                    } catch (InvalidRuleException | AmbiguitiesException e) {
                        LoggerHandler.info(e.getMessage());

                        json.put("status", "illegal");

                    }
                    response.datas.add(json);
                    break;
                }
            }
        }
        session.sendMessage(new TextMessage(response.response("modifyFormula")));
    }

    private static void deleteFormula(WebSocketSession session, JSONArray params, JSONObject user) throws IOException {
        List<Formula> formulas = getFormula(user);
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
        session.sendMessage(new TextMessage(response.response("deleteFormula")));
        FocusParser fp = (FocusParser) user.get("parser");
        ModelBuild.deleteFormulas(fp, formulaNames);
    }

    private static void formulaCase(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        String keyword = params.getString("keyword");
        FormulaCaseResponse response = new FormulaCaseResponse();
        //        {"keyword":"+", "cases":["4 + 1", "47.69 + 99.34", "userid + userid"]}
        JSONObject datas = new JSONObject();
        datas.put("keyword", keyword);
        datas.put("cases", FormulaCase.buildCase(user, keyword));
        response.setDatas(datas);
        session.sendMessage(new TextMessage(response.response()));
    }

    private static void echo(WebSocketSession session, JSONObject user) throws IOException {
        JSONObject response = new JSONObject();
        response.put("type", "echo");
        session.sendMessage(new TextMessage(response.toJSONString()));
        FocusParser fp = (FocusParser) user.get("parser");
        String msg;
        if (fp != null) {
            msg = JSON.toJSONString(fp.getTerminalTokens());
        } else {
            msg = "null";
        }
        session.sendMessage(new TextMessage(msg));
    }

    // textChange
    private static void textChange() {

    }

    // focusIn
    private static void focusIn(WebSocketSession session, String question, int position, FocusParser fp) throws IOException {
        session.sendMessage(new TextMessage(JSON.toJSONString(fp.getTerminalTokens())));
    }

    // move
    private static void move() {

    }

    //  search 输出返回结果
    private static void response(WebSocketSession session, String search, JSONObject user) throws IOException {
        // 接收请求的时间戳
        long received = Long.parseLong(session.getAttributes().get(WebsocketSearch.RECEIVED_TIMESTAMP).toString());

        FocusParser fp = (FocusParser) user.get("parser");
        String category = user.getString("category");
        String language = user.getString("language");

        boolean isQuestion = Constant.CategoryType.QUESTION.equalsIgnoreCase(category);

        // 分词
        List<FocusToken> tokens = fp.focusAnalyzer.test(search, language);
        System.out.println("split words:" + JSON.toJSONString(tokens));

        if (tokens.size() == 0) {
            errorResponse(session, search, user);
            return;
        }

        JSONObject amb = user.getJSONObject("ambiguities");

        try {
            // 解析结果
            FocusInst focusInst;
            if (isQuestion) {
                focusInst = fp.parseQuestion(tokens, amb);
            } else {
                focusInst = fp.parseFormula(tokens, amb);
            }

            System.out.println(focusInst.toJSON().toJSONString());

            String msg;
            if (focusInst.position < 0) {// 未出错
                FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
                if (focusPhrase.isSuggestion()) {// 出入不完整
                    SuggestionResponse response = new SuggestionResponse(search);
                    SuggestionResponse.Datas datas = new SuggestionResponse.Datas();
                    JSONObject json = SuggestionBuild.sug(tokens, focusInst);
                    datas.beginPos = json.getInteger("position");
                    datas.phraseBeginPos = datas.beginPos;
                    List<FocusNode> focusNodes = JSONArray.parseArray(json.getJSONArray("suggestions").toJSONString(), FocusNode.class);
                    focusNodes.forEach(node -> {
                        SuggestionResponse.Suggestions suggestion = new SuggestionResponse.Suggestions();
                        suggestion.suggestion = node.getValue();
                        suggestion.suggestionType = node.getType();
                        if (Constant.FNDType.TABLE.equalsIgnoreCase(node.getType())) {
                            suggestion.description = "this is a table name";
                        } else if (Constant.FNDType.COLUMN.equalsIgnoreCase(node.getType())) {
                            Column col = node.getColumn();
                            suggestion.description = "column '" + node.getValue() + "' in table '" + col.getSourceName() + "'";
                        }
                        datas.suggestions.add(suggestion);
                    });
                    response.setDatas(datas);
                    session.sendMessage(new TextMessage(response.response()));
                    System.out.println("提示:\n\t" + JSON.toJSONString(focusNodes) + "\n");
                } else {//  输入完整

                    if (!isQuestion) {// formula
                        FormulaResponse response = new FormulaResponse(search);
                        FormulaAnalysis.FormulaObj formulaObj = FormulaAnalysis.analysis(focusInst.lastFocusPhrase());
                        FormulaResponse.Datas datas = new FormulaResponse.Datas();
                        datas.settings = FormulaAnalysis.getSettings(formulaObj);
                        datas.formulaObj = formulaObj.toString();
                        response.setDatas(datas);
                        session.sendMessage(new TextMessage(response.response()));
                        //  todo here
//                        {"type":"state","question":"id+9","cost":27,"icount":0,"icost":0,"iRequestTime":0,"pcost":9,"datas":"searchFinished"}
                        session.sendMessage(new TextMessage(SearchFinishedResponse.response(search, received)));
                        return;
                    }

                    StateResponse response = new StateResponse(search);
                    // 生成指令
                    response.setDatas("prepareQuery");
                    session.sendMessage(new TextMessage(response.response()));
                    JSONObject json = InstructionBuild.build(focusInst, search, amb, getFormula(user));

                    json.put("source", "searchUser");
                    json.put("sourceToken", user.getString("sourceToken"));

                    System.out.println("指令:\n\t" + json + "\n");

                    // Annotations
                    AnnotationResponse annotationResponse = new AnnotationResponse(search);
                    JSONArray instructions = json.getJSONArray("instructions");
                    for (int i = 0; i < instructions.size(); i++) {
                        JSONObject instruction = instructions.getJSONObject(i);
                        if (instruction.getString("instId").equals("annotation")) {
                            String content = instruction.getString("content");
                            annotationResponse.datas.add(JSONObject.parseObject(content, AnnotationResponse.Datas.class));
                        }
                    }
                    session.sendMessage(new TextMessage(annotationResponse.response()));

                    //  todo here
//                    {"type":"state","question":"id","cost":16,"icount":0,"icost":0,"iRequestTime":0,"pcost":7,"datas":"searchFinished"}
                    session.sendMessage(new TextMessage(SearchFinishedResponse.response(search, received)));

                    // 指令检测
                    response.setDatas("precheck");
                    session.sendMessage(new TextMessage(response.response()));
                    // todo 发送BI检测指定是否可执行

                    // 指令检测完毕
                    response.setDatas("precheckDone");
                    session.sendMessage(new TextMessage(response.response()));

                    // 准备执行指令
                    response.setDatas("executeQuery");
                    session.sendMessage(new TextMessage(response.response()));

                    // todo 给BI发送指令，获取查询结果
                    JSONObject res = Clients.Bi.query(json.toJSONString());
                    String taskId = res.getString("taskId");
                    session.getAttributes().put("taskId", taskId);

                    // todo 同步返回，测试用
                    json.put("query_type", "synchronize");
                    JSONObject res1 = Clients.Bi.query(json.toJSONString());
                    ChartsResponse chartsResponse = new ChartsResponse(json.getString("question"), json.getString("sourceToken"));
                    chartsResponse.setDatas(res1);
                    session.sendMessage(new TextMessage(chartsResponse.response()));

                }
            } else {//  出错
                IllegalResponse response = new IllegalResponse(search);
                int strPosition = tokens.get(focusInst.position).getStart();
                IllegalResponse.Datas datas = new IllegalResponse.Datas();
                datas.beginPos = strPosition;
                StringBuilder reason = new StringBuilder();
                if (focusInst.position == 0) {
                    List<Column> cols = SuggestionBuild.colRandomSuggestions(user);
                    cols.forEach(col -> {
                        reason.append(col.getColumnDisplayName()).append(",").append(Constant.FNDType.COLUMN);
                        reason.append(",").append(col.getColumnId()).append("\r\n");
                    });
                } else {
                    List<FocusNode> focusNodes = SuggestionBuild.sug(focusInst.position, focusInst);
                    focusNodes.forEach(node -> {
                        reason.append(node.getValue());
                        if (node.getType() != null) {
                            reason.append(",").append(node.getType());
                        }
                        if (node.getColumn() != null) {
                            reason.append(",").append(node.getColumn().getColumnId());
                        }
                        reason.append("\r\n");
                    });
                }
                datas.reason = reason.toString();
                response.setDatas(datas);
                session.sendMessage(new TextMessage(response.response()));
                msg = "错误:\n\t" + "位置: " + strPosition + "\t错误: " + search.substring(strPosition) + "\n";
                System.out.println(msg);
                msg = "提示:\n\t" + reason + "\n";
                System.out.println(msg);
            }

        } catch (InvalidRuleException e) {
            e.printStackTrace();
        } catch (AmbiguitiesException e) {
            AmbiguityResponse response = new AmbiguityResponse(search);
            FocusToken ft = tokens.get(e.position);
            AmbiguityResponse.Datas datas = new AmbiguityResponse.Datas();
            datas.begin = ft.getStart();
            datas.end = ft.getEnd();
            datas.id = UUID.randomUUID().toString();
            datas.title = "ambiguity word: " + ft.getWord();
            e.ars.forEach(a -> datas.possibleMenus.add(a.columnName + " in table " + a.sourceName));
            response.setDatas(datas);
            session.sendMessage(new TextMessage(response.response()));
            System.out.println(response.response());

            AmbiguitiesResolve ar = new AmbiguitiesResolve();
            ar.ars = e.ars;
            ar.value = ft.getWord();
            amb.put(datas.id, ar);
            user.put("ambiguities", amb);

//            session.getAttributes().put("user", user);

        } catch (Exception e) {
            String exc = e.getMessage();
            session.sendMessage(new TextMessage(exc == null ? "something error" : exc));
        }
    }

    private static void errorResponse(WebSocketSession session, String search, JSONObject user) throws IOException {
        SuggestionResponse response = new SuggestionResponse(search);
        SuggestionResponse.Datas datas = new SuggestionResponse.Datas();
        datas.beginPos = 0;
        datas.phraseBeginPos = datas.beginPos;

        List<Column> columns = SuggestionBuild.colRandomSuggestions(user);
        for (Column column : columns) {
            SuggestionResponse.Suggestions suggestions = new SuggestionResponse.Suggestions();
            suggestions.suggestion = column.getColumnDisplayName();
            suggestions.suggestionType = Constant.FNDType.COLUMN;
            suggestions.description = column.getColumnDisplayName() + " in table " + column.getSourceName();
            datas.suggestions.add(suggestions);
        }
        response.setDatas(datas);
        session.sendMessage(new TextMessage(response.response()));
        System.out.println("提示:\n\t" + response.response() + "\n");
    }

    private static String fnamecheck(String name, String id, JSONObject user) {
        FocusParser fp = (FocusParser) user.get("parser");
        // 系统关键词
        // 1. 符号
        List<TerminalToken> tokens = SuggestionBuild.terminalTokens(fp, "<symbol>");
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
        List<Formula> formulas = getFormula(user);
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

    private static List<Formula> getFormula(JSONObject user) {
        List<Formula> formulas;
        Object obj = user.get("formulas");
        if (obj == null) {
            formulas = new ArrayList<>();
        } else {
            formulas = (List<Formula>) obj;
        }
        return formulas;
    }

}
