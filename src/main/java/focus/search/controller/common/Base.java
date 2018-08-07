package focus.search.controller.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.base.LanguageUtils;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusParser;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.controller.WebsocketSearch;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.InstructionBuild;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.meta.*;
import focus.search.metaReceived.Ambiguities;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.exception.*;
import focus.search.response.search.*;
import focus.search.suggestions.HistoryUtils;
import focus.search.suggestions.SourcesUtils;
import focus.search.suggestions.SuggestionUtils;
import org.apache.log4j.Logger;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * creator: sunc
 * date: 2018/5/7
 * description: control 中的一些公共方法
 */
public class Base {
    private static final Logger logger = Logger.getLogger(Base.class);

    private static final Integer WebsocketLimit = 100;

    public static final FocusParser englishParser;
    public static final FocusParser chineseParser;

    static {
        englishParser = new FocusParser(Constant.Language.ENGLISH);
        chineseParser = new FocusParser(Constant.Language.CHINESE);
    }

    /**
     * 根据context恢复歧义/语言 等
     *
     * @param contextJson json 类型的context
     * @param srs         source tables
     * @return json 格式的歧义
     */
    public static JSONObject context(JSONObject contextJson, List<SourceReceived> srs) {
        JSONObject ambiguities = new JSONObject();
        String contextStr = contextJson.getString("disambiguitions");// 检测 lisp 返回的空值 nil
        if (!Common.isEmpty(contextStr) && !contextStr.equalsIgnoreCase("NIL")) {

            // 恢复歧义
            JSONArray disambiguitions = JSONArray.parseArray(contextStr);

            for (Object obj : disambiguitions) {
                AmbiguitiesRecord record = JSON.parseObject(obj.toString(), AmbiguitiesRecord.class);
                AmbiguitiesResolve ar = new AmbiguitiesResolve();
                ar.value = record.realValue;
                ar.isResolved = true;

                List<Column> columns = CommonFunc.getColumns(record.realValue, srs);
                ar.ars.addAll(getRecords(columns));
                ar.addRecord(record);
                for (AmbiguitiesRecord a : ar.ars) {
                    if (a.equals(record)) {
                        ar.ars.remove(a);
                        ar.ars.add(0, a);
                        break;
                    }
                }

                ambiguities.put(UUID.randomUUID().toString(), ar);
            }
        }
        return ambiguities;
    }

    /**
     * 根据 search 参数 ambiguities 恢复歧义
     *
     * @param ambiguities 歧义列表
     * @param srs         source tables
     * @param amb         已经存在的歧义列表
     */
    public static void ambiguities(List<Ambiguities> ambiguities, List<SourceReceived> srs, JSONObject amb) {
        for (Ambiguities obj : ambiguities) {
            if (!obj.type.equalsIgnoreCase(Constant.FNDType.COLUMN)) {
                continue;
            }

            String columnName = obj.name;
            AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getByValue(columnName, amb);
            if (ambiguitiesResolve == null) {
                ambiguitiesResolve = new AmbiguitiesResolve();

                int columnId = obj.id;
                AmbiguitiesResolve ar = new AmbiguitiesResolve();
                ar.value = columnName;
                ar.isResolved = true;

                List<Column> columns = CommonFunc.getColumns(columnName, srs);
                ar.ars.addAll(getRecords(columns));
                for (AmbiguitiesRecord a : ar.ars) {
                    if (a.columnId == columnId) {
                        ar.ars.remove(a);
                        ar.ars.add(0, a);
                        break;
                    }
                }
                amb.put(UUID.randomUUID().toString(), ambiguitiesResolve);
            }
        }
    }

    private static List<AmbiguitiesRecord> getRecords(List<Column> columns) {
        List<AmbiguitiesRecord> records = new ArrayList<>();
        for (Column column : columns) {
            AmbiguitiesRecord ambiguitiesRecord = new AmbiguitiesRecord();
            ambiguitiesRecord.sourceName = column.getSourceName();
            ambiguitiesRecord.columnName = column.getColumnDisplayName();
            ambiguitiesRecord.columnId = column.getColumnId();
            ambiguitiesRecord.type = Constant.AmbiguityType.COLUMN;
            ambiguitiesRecord.realValue = ambiguitiesRecord.columnName;
            ambiguitiesRecord.possibleValue = ambiguitiesRecord.columnName;
            records.add(ambiguitiesRecord);
        }
        return records;
    }

    /**
     * @param session websocket session
     * @param search  需要解析的question
     * @param user    websocket用户信息
     * @throws IOException 异常
     */
    //  search 输出返回结果
    public static void response(WebSocketSession session, String search, JSONObject user, List<Ambiguities> ambiguities, int position) throws IOException, FocusHttpException,
            FocusParserException, FocusInstructionException, IllegalException {
        response(session, search, user, ambiguities, null, position, null);
    }

    /**
     * @param session     websocket session
     * @param search      需要解析的question
     * @param user        websocket用户信息
     * @param ambiguities 参数歧义列表
     * @throws IOException 异常
     */
    //  search 输出返回结果
    public static void response(WebSocketSession session, String search, JSONObject user, List<Ambiguities> ambiguities, String event, int position, JSONObject biConfig) throws IOException, FocusHttpException, FocusParserException, FocusInstructionException, IllegalException {
        // 接收请求的时间戳
        long received = Long.parseLong(session.getAttributes().get(WebsocketSearch.RECEIVED_TIMESTAMP).toString());

        FocusParser fp = (FocusParser) user.get("parser");
        String category = user.getString("category");
        String language = user.getString("language");

        boolean isQuestion = Constant.CategoryType.QUESTION.equalsIgnoreCase(category);

        if (Common.isEmpty(search.trim())) {
            SuggestionResponse response = SuggestionUtils.suggestionsNull(fp, user, search, search.length());
            Common.send(session, response.response());
            logger.info("提示:\n\t" + response.response() + "\n");
            return;
        }
        JSONObject amb = user.getJSONObject("ambiguities");

        logger.info("current ambiguities:" + amb);

        String ambiguityTitle = LanguageUtils.getMsg(language, LanguageUtils.Ambiguity_title);
        String ambiguityItem = LanguageUtils.getMsg(language, LanguageUtils.Ambiguity_item);

        // 分词  中文分词会出现歧义
        List<FocusToken> tokens;
        try {
            tokens = fp.focusAnalyzer.test(search, language);
        } catch (AmbiguitiesException e) {
            AmbiguityResponse response = new AmbiguityResponse(search);
            String ambiguityWord = search.substring(e.begin, e.end);
            String id = AmbiguitiesResolve.mergeAmbiguities(e.ars, ambiguityWord, amb);
            user.put("ambiguities", amb);

            AmbiguityDatas datas = new AmbiguityDatas();
            datas.begin = e.begin;
            datas.end = e.end;
            datas.id = id;
            datas.title = String.format(ambiguityTitle, ambiguityWord);
            e.ars.forEach(a -> datas.possibleMenus.add(a.possibleValue));
            response.setDatas(datas);
            Common.send(session, response.response());
            logger.info(response.response());

            Common.send(session, SearchFinishedResponse.response(search, received));
            return;
        }
        logger.info("split words:" + JSON.toJSONString(tokens));

        logger.info("lastParams ambiguities:" + ambiguities);
        @SuppressWarnings("unchecked")
        List<SourceReceived> srs = (List<SourceReceived>) user.get("sources");
        if (ambiguities != null) {
            ambiguities(ambiguities, srs, amb);
            user.put("ambiguities", amb);
        }

        if (tokens.size() == 1) {
            FocusToken input = tokens.get(0);
            if (!Constant.FNDType.INTEGER.equals(input.getType()) && !Constant.FNDType.DOUBLE.equals(input.getType()) && !fp.isKeyword(input.getWord())) {
                // 出错或者不完整的keyword
                List<TerminalToken> terminalTokens = fp.getTerminalTokens();
                List<SuggestionSuggestion> sss = new ArrayList<>();
                for (TerminalToken t : terminalTokens) {
                    if (t.getName().startsWith(input.getWord())) {
                        SuggestionSuggestion ss = new SuggestionSuggestion();
                        ss.beginPos = 0;
                        ss.endPos = position;
                        ss.suggestion = t.getName();
                        ss.suggestionType = t.getType();
                        sss.add(ss);
                    }
                }
                if (sss.isEmpty()) {
                    IllegalResponse response = new IllegalResponse(search);
                    IllegalDatas datas = new IllegalDatas();
                    datas.beginPos = 0;
                    String reason = LanguageUtils.getMsg(language, LanguageUtils.IllegalDatas_reason);
                    String reasonItem = LanguageUtils.getMsg(language, LanguageUtils.IllegalDatas_reason_item);
                    SuggestionResponse suggestionResponse = SuggestionUtils.suggestionsNull(fp, user, search, search.length());
                    StringBuilder sb = new StringBuilder();
                    suggestionResponse.getDatas().suggestions.forEach(s -> sb.append(String.format(reasonItem, s.suggestion)));
                    datas.reason = String.format(reason, sb.toString());
                    response.setDatas(datas);
                    Common.send(session, response.response());
                } else {
                    SuggestionDatas datas = new SuggestionDatas();
                    datas.addAllSug(sss);
                    SuggestionResponse response = new SuggestionResponse(search);
                    response.setDatas(datas);
                    Common.send(session, response.response());
                }

                // search finish
                Common.send(session, SearchFinishedResponse.response(search, received));
                return;
            }

        }
        List<Formula> formulas = Base.getFormula(user);
        try {
            // 解析结果
            FocusInst focusInst;
            logger.info(String.format("isQuestion:%s tokens:%s ambiguities:%s language:%s", isQuestion, JSON.toJSONString(tokens), amb, language));
            if (isQuestion) {
                focusInst = fp.parseQuestion(tokens, amb, language, srs, formulas);
            } else {
                focusInst = fp.parseFormula(tokens, amb, language, srs);
            }

//            logger.info(focusInst.toJSON().toJSONString());

            if (focusInst.position < 0) {// 未出错

                if (!focusInst.isInstruction) {// 出入不完整
                    SuggestionResponse response = SuggestionUtils.suggestionsNotCompleted(fp, search, focusInst, user, tokens, search.length());

                    // search suggestions
                    if (response != null)
                        Common.send(session, response.response());

                    // search finish
                    Common.send(session, SearchFinishedResponse.response(search, received));
                } else {//  输入完整
                    if (!isQuestion) {// formula
                        // 生成suggestion
                        SuggestionResponse sug = SuggestionUtils.suggestionsCompleted(fp, search, focusInst, user, tokens, position);
                        if (sug != null)
                            Common.send(session, sug.response());
                        FormulaResponse response = new FormulaResponse(search);

                        // 获取日期列
                        List<Column> dateColumns = SourcesUtils.colRandomSuggestions(user, Constant.DataType.TIMESTAMP);
                        FocusPhrase formula = focusInst.firstFocusPhrase();
                        if (formula == null) {
                            Common.send(session, FatalResponse.response("focusPhrase is null", session.getAttributes().get("sourceToken").toString()));
                            return;
                        }
                        FormulaAnalysis.FormulaObj formulaObj = FormulaAnalysis.analysis(formula, amb, language, dateColumns);
                        logger.info(formulaObj.toJSON());
                        FormulaDatas datas = new FormulaDatas();
                        datas.settings = FormulaAnalysis.getSettings(formula);
                        datas.formulaObj = formulaObj.toString();
                        response.setDatas(datas);
                        Common.send(session, response.response());
                        Common.send(session, SearchFinishedResponse.response(search, received));
                        return;
                    }

                    StateResponse response = new StateResponse(search);

                    // 获取日期列
                    List<Column> dateColumns = SourcesUtils.colRandomSuggestions(user, Constant.DataType.TIMESTAMP);
                    // 生成指令
                    JSONObject json = InstructionBuild.build(focusInst, search, amb, formulas, language, dateColumns);

                    JSONArray instructions = json.getJSONArray("instructions");
                    if (biConfig != null && !biConfig.isEmpty()) {
                        JSONObject config = new JSONObject();
                        config.put("instId", Constant.InstIdType.SET_BI_CONFIG);
                        config.put("value", biConfig);
                        instructions.add(config);
                        json.put("instructions", instructions.toJSONString());
                    }

                    json.put("source", Constant.SearchOrPinboard.SEARCH_USER); // 区分是search框还是pinboard
                    json.put("sourceToken", user.getString("sourceToken"));

                    logger.info("指令:\n\t" + json + "\n");
                    // Annotations
                    AnnotationResponse annotationResponse = new AnnotationResponse(search);
                    annotationResponse.datas.addAll(getAnnotationDatas(instructions));
                    Common.send(session, annotationResponse.response());

                    // 生成suggestion
                    SuggestionResponse sug = SuggestionUtils.suggestionsCompleted(fp, search, focusInst, user, tokens, position);
                    if (sug != null)
                        Common.send(session, sug.response());

                    // search finish
                    Common.send(session, SearchFinishedResponse.response(search, received));

                    if (event != null && !Constant.Event.TEXT_CHANGE.equalsIgnoreCase(event)) {
                        return;
                    }
                    // prepareQuery
                    response.setDatas("prepareQuery");
                    Common.send(session, response.response());

                    // 指令检测
                    response.setDatas("precheck");
                    Common.send(session, response.response());

                    if (checkQuery(session, json, search)) {
                        logger.debug("precheck fail.");
                        return;
                    }

                    // 指令检测完毕
                    response.setDatas("precheckDone");
                    Common.send(session, response.response());

                    // 准备执行指令
                    response.setDatas("executeQuery");
                    Common.send(session, response.response());

                    JSONObject res = Clients.Bi.query(json.toJSONString());
                    logger.debug("executeQuery result:" + res);
                    String taskId = res.getString("taskId");
                    session.getAttributes().put("taskId", taskId);
                    QuartzManager.addJob(taskId, session);
                    FocusPhrase tmp;
                    List<FocusNode> nodes;
                    boolean add = true;
                    for (FocusPhrase focusPhrase : focusInst.getFocusPhrases()) {
                        if (focusPhrase.isSuggestion() || !add) {
                            break;
                        }
                        tmp = focusPhrase.getNode(0).getChildren();
                        if ("<string-simple-filter>".equals(tmp.getInstName())) {
                            nodes = tmp.getFocusNodes();
                            Column column = nodes.get(0).getChildren().getLastNode().getColumn();
                            FocusPhrase columnValue = nodes.get(2).getChildren();
                            try {
                                List<FocusNode> columnValueNodes = columnValue.getFocusNodes();
                                for (int i = 0; i < columnValueNodes.size(); i = i + 2) {
                                    FocusNode columnValueNode = columnValueNodes.get(i);
                                    JSONObject result = Clients.Index.tokens(column, columnValueNode.getChildren().getNodeNew(1).getValue(), 10);
                                    JSONArray jsonArray = result.getJSONArray("tokens");
                                    if (jsonArray.isEmpty()) {
                                        add = false;
                                        break;
                                    }
                                }
                            } catch (FocusHttpException e) {
                                logger.error(Common.printStacktrace(e));
                                add = false;
                                break;
                            }
                        }
                    }
                    if (add) {// 含有列中值并且列中值不存在的question，不记录到history中
                        // 添加到历史记录中,并且放弃上一次搜索
                        HistoryUtils.addQuestion(new HistoryQuestion(tokens, user.getString("sourceList"), taskId), user);
                    }
                }
            } else {//  出错
                IllegalResponse response = new IllegalResponse(search);
                int strPosition = tokens.get(focusInst.position).getStart();
                IllegalDatas datas = new IllegalDatas();
                datas.beginPos = strPosition;
                String reason = LanguageUtils.getMsg(language, LanguageUtils.IllegalDatas_reason);
                String reasonItem = LanguageUtils.getMsg(language, LanguageUtils.IllegalDatas_reason_item);
                if (focusInst.position == 0) {
                    SuggestionResponse suggestionResponse = SuggestionUtils.suggestionsNull(fp, user, search, search.length());
                    StringBuilder sb = new StringBuilder();
                    suggestionResponse.getDatas().suggestions.forEach(s -> sb.append(String.format(reasonItem, s.suggestion)));
                    datas.reason = String.format(reason, sb.toString());
                    response.setDatas(datas);
                    Common.send(session, response.response());
                    logger.info(response.response());
                } else {
                    int errorTokenIndex = focusInst.position;
                    int beginPos = tokens.get(errorTokenIndex).getStart();
                    if (beginPos > position) {//光标在未出错部分的search中间的时候，给出提示
                        SuggestionResponse suggestionResponse = SuggestionUtils.suggestionsMiddleError(fp, search, user, tokens, position);
                        Common.send(session, suggestionResponse.response());
                    } else {
                        List<SuggestionSuggestion> sss = SuggestionUtils.suggestionsMiddleError(fp, search, focusInst, user, tokens);
                        StringBuilder sb = new StringBuilder();
                        sss.forEach(s -> sb.append(String.format(reasonItem, s.suggestion)));
                        datas.reason = String.format(reason, sb.toString());
                        response.setDatas(datas);
                        Common.send(session, response.response());
                        logger.info(response.response());
                    }
                }
            }

        } catch (
                AmbiguitiesException e)

        {
            AmbiguityResponse response = new AmbiguityResponse(search);

            String title;
            String ambiguityWord;
            if (e.position < 0) {
                ambiguityWord = Constant.AmbiguityType.getWord(e.position);
                title = search.substring(e.begin, e.end);
            } else {
                ambiguityWord = tokens.get(e.position).getWord();
                title = ambiguityWord;
            }

            String id = AmbiguitiesResolve.mergeAmbiguities(e.ars, ambiguityWord, amb);
            user.put("ambiguities", amb);

            AmbiguityDatas datas = new AmbiguityDatas();
            datas.begin = e.begin;
            datas.end = e.end;
            datas.id = id;
            datas.title = String.format(ambiguityTitle, title);
            e.ars.forEach(a -> {
                String value1 = a.columnName;
                String value2 = a.sourceName;
                if (Constant.Language.CHINESE.equals(language)) {
                    value1 = a.sourceName;
                    value2 = a.columnName;
                }
                String value = String.format(ambiguityItem, value1, value2);
                datas.possibleMenus.add(value);
            });
            response.setDatas(datas);
            Common.send(session, response.response());
            logger.info(response.response());

            Common.send(session, SearchFinishedResponse.response(search, received));

        } catch (
                IllegalException e)

        {
            e.question = search;
            throw e;
        }

    }

    /**
     * @param user websocket用户信息
     * @return 返回用户中的公式列表
     */
    @SuppressWarnings("unchecked")
    public static List<Formula> getFormula(JSONObject user) {
        List<Formula> formulas;
        Object obj = user.get("formulas");
        if (obj == null) {
            formulas = new ArrayList<>();
        } else {
            formulas = (List<Formula>) obj;
        }
        return formulas;
    }

    private static String getLastQuestion(JSONArray questions) {
        if (questions.size() > 0) {
            HistoryQuestion last = (HistoryQuestion) questions.get(0);
            return last.question;
        }
        return "";
    }

    /**
     * 检测 answer 依赖列的修改是否影响 answer
     *
     * @param columns 所有修改的列
     * @param col     answer 依赖的列
     * @return bool
     */
    public static boolean affect(JSONArray columns, Column col) {
        for (int i = 0; i < columns.size(); i++) {
            JSONObject column = columns.getJSONObject(i);
            if (col.getColumnId() == column.getInteger("id")) {
                return !col.getColumnDisplayName().equalsIgnoreCase(column.getString("columnDisplayName"));
            }
        }
        return false;
    }

    /**
     * @param accessToken userCenter accessToken
     * @return is login
     * @throws FocusHttpException http exception
     */
    public static boolean isLogin(String accessToken) throws FocusHttpException {
        return Constant.passUc || Clients.Uc.isLogin(accessToken);
    }

    /**
     * @param session websocket session
     * @param json    指令参数
     * @return 是否停止执行
     * @throws IOException sendMessage 异常
     */
    public static boolean checkQuery(WebSocketSession session, JSONObject json, String question) throws IOException {
        JSONObject checkQuery = new JSONObject();
        try {
            checkQuery = Clients.Bi.checkQuery(json.toJSONString());
            if (!checkQuery.getBooleanValue("success")) {
                IllegalDatas datas = new IllegalDatas(0, question.length() - 1, checkQuery.getString("exception"));
                IllegalResponse illegal = new IllegalResponse(question, datas);
                Common.send(session, illegal.response());
                return true;
            }
        } catch (FocusHttpException e) {
            IllegalDatas datas = new IllegalDatas(0, question.length() - 1, checkQuery.getString("exception"));
            IllegalResponse illegal = new IllegalResponse(question, datas);
            Common.send(session, illegal.response());
            return true;
        }
        return false;
    }

    public static List<AnnotationDatas> getAnnotationDatas(JSONArray instructions) {
        List<AnnotationDatas> datas = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            JSONObject instruction = instructions.getJSONObject(i);
            if (instruction.getString("instId").equals(Constant.InstIdType.ANNOTATION)) {
                String content = instruction.getString("content");
                datas.add(JSONObject.parseObject(content, AnnotationDatas.class));
            }
        }
        return datas;
    }

    public static String InstName(FocusPhrase fp) {
        StringBuilder instName = new StringBuilder();
        FocusNode first = fp.getNodeNew(0);
        int space = first.getEnd();
        instName.append(first.getValue());
        for (int i = 1; i < fp.size(); i++) {
            FocusNode fn = fp.getNodeNew(i);
            instName.append(space(fn.getBegin() - space));
            instName.append(fn.getValue());
            space = fn.getEnd();
        }
        return instName.toString();
    }

    public static String space(int count) {
        StringBuilder space = new StringBuilder("");
        int i = 0;
        while (i++ < count) {
            space.append(" ");
        }
        return space.toString();
    }

    private static JSONObject userInfo(String accessToken) throws FocusHttpException {
        if (Constant.passUc) {
            JSONObject user = new JSONObject();
            user.put("id", 1);
            user.put("accessToken", accessToken);
            user.put("name", "admin");
            user.put("username", "admin");
            user.put("privileges", Collections.singletonList("[\"ADMIN\"]"));
            return user;
        }
        return Clients.Uc.getUserInfo(accessToken);
    }

    public static void afterConnectionEstablished(List<WebSocketSession> users, WebSocketSession session) throws IOException {
        if (users.size() >= Base.WebsocketLimit) {
            String warn = "Websocket connected too much.";
            logger.warn(warn);
            Common.send(session, warn);
            if (session.isOpen())
                session.close();
            return;
        }
        String accessToken = session.getAttributes().get("accessToken").toString();
        logger.info("current accessToken:" + accessToken);
        JSONObject user;
        try {
            // 获取用户信息
            user = Base.userInfo(accessToken);
            user.put("accessToken", accessToken);
        } catch (FocusHttpException e) {
            logger.error(Common.printStacktrace(e));
            String warn = ErrorResponse.response(Constant.ErrorType.ERROR, "Get Userinfo fail.").toJSONString();
            Common.send(session, warn);
            session.close();
            return;
        }
        session.getAttributes().put("user", user);
        logger.info(user.getString("name") + " connected to server.");
        users.add(session);
    }

}
