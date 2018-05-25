package focus.search.controller.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusParser;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.WebsocketSearch;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.InstructionBuild;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.meta.*;
import focus.search.metaReceived.Ambiguities;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.exception.*;
import focus.search.response.search.*;
import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * creator: sunc
 * date: 2018/5/7
 * description: control 中的一些公共方法
 */
public class Base {
    private static final Logger logger = Logger.getLogger(Base.class);

    public static final Integer WebsocketLimit = 100;

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
        String contextStr = contextJson.getString("disambiguations");// 检测 lisp 返回的空值 nil
        if (!Common.isEmpty(contextStr) && !contextStr.equalsIgnoreCase("NIL")) {

            // 恢复歧义
            JSONArray disambiguations = JSONArray.parseArray(contextStr);

            for (Object obj : disambiguations) {
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
    public static void response(WebSocketSession session, String search, JSONObject user) throws IOException, FocusHttpException, FocusParserException, FocusInstructionException, IllegalException {
        response(session, search, user, null, null);
    }

    /**
     * @param session     websocket session
     * @param search      需要解析的question
     * @param user        websocket用户信息
     * @param ambiguities 参数歧义列表
     * @throws IOException 异常
     */
    //  search 输出返回结果
    public static void response(WebSocketSession session, String search, JSONObject user, List<Ambiguities> ambiguities, String event) throws
            IOException, FocusHttpException, FocusParserException, FocusInstructionException, IllegalException {
        // 接收请求的时间戳
        long received = Long.parseLong(session.getAttributes().get(WebsocketSearch.RECEIVED_TIMESTAMP).toString());

        FocusParser fp = (FocusParser) user.get("parser");
        String category = user.getString("category");
        String language = user.getString("language");

        boolean isQuestion = Constant.CategoryType.QUESTION.equalsIgnoreCase(category);

        if (Common.isEmpty(search)) {
            // TODO: 2018/5/16 modify suggestions
            SuggestionResponse response = new SuggestionResponse(search);
            SuggestionDatas datas = new SuggestionDatas();
            datas.beginPos = 0;
            datas.phraseBeginPos = datas.beginPos;

            JSONArray historyQuestions = user.getJSONArray("historyQuestions");
            for (Object history : historyQuestions) {
                SuggestionSuggestion suggestions = new SuggestionSuggestion();
                suggestions.suggestion = history.toString();
                suggestions.suggestionType = "history";
                suggestions.description = "history";
                datas.suggestions.add(suggestions);
            }

            List<Column> columns = SuggestionBuild.colRandomSuggestions(user);
            for (Column column : columns) {
                SuggestionSuggestion suggestions = new SuggestionSuggestion();
                suggestions.suggestion = column.getColumnDisplayName();
                suggestions.suggestionType = Constant.FNDType.COLUMN;
                suggestions.description = column.getColumnDisplayName() + " in table " + column.getSourceName();
                datas.suggestions.add(suggestions);
            }
            response.setDatas(datas);
            session.sendMessage(new TextMessage(response.response()));
            logger.info("提示:\n\t" + response.response() + "\n");
            return;
        }
        JSONObject amb = user.getJSONObject("ambiguities");

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
            datas.title = "ambiguity word: " + ambiguityWord;
            e.ars.forEach(a -> datas.possibleMenus.add(a.possibleValue));
            response.setDatas(datas);
            session.sendMessage(new TextMessage(response.response()));
            logger.info(response.response());

            session.sendMessage(new TextMessage(SearchFinishedResponse.response(search, received)));
            return;
        }
        logger.info("split words:" + JSON.toJSONString(tokens));

        if (ambiguities != null) {
            @SuppressWarnings("unchecked")
            List<SourceReceived> srs = (List<SourceReceived>) user.get("sources");
            ambiguities(ambiguities, srs, amb);
            user.put("ambiguities", amb);
        }

        try {
            // 解析结果
            FocusInst focusInst;
            if (isQuestion) {
                logger.info("search question. tokens:" + JSON.toJSONString(tokens) + " ambiguities:" + amb);
                focusInst = fp.parseQuestion(tokens, amb);
            } else {
                focusInst = fp.parseFormula(tokens, amb);
            }

            logger.info(focusInst.toJSON().toJSONString());

            String msg;
            if (focusInst.position < 0) {// 未出错
                int n = tokens.size();
                for (FocusPhrase f : focusInst.getFocusPhrases()) {
                    for (int i = 0; i < f.size(); i++) {
                        if (n <= 0) {
                            break;
                        }
                        FocusNode node = f.getNodeNew(i);
                        if (Constant.FNDType.COLUMN.equals(node.getType())) {
                            Column col = node.getColumn();
                            AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getByValue(col.getColumnDisplayName(), amb);
                            if (ambiguitiesResolve == null) {
                                ambiguitiesResolve = new AmbiguitiesResolve();

                                AmbiguitiesRecord ar = new AmbiguitiesRecord();
                                ar.type = Constant.AmbiguityType.COLUMN;
                                ar.sourceName = col.getSourceName();
                                ar.columnId = col.getColumnId();
                                ar.columnName = col.getColumnDisplayName();
                                ar.realValue = ar.columnName;
                                ar.possibleValue = ar.columnName;

                                ambiguitiesResolve.ars.add(0, ar);
                                ambiguitiesResolve.isResolved = true;
                                ambiguitiesResolve.value = col.getColumnName();
                                amb.put(UUID.randomUUID().toString(), ambiguitiesResolve);
                            }
                        }
                        n--;
                    }
                    if (n <= 0) {
                        break;
                    }
                }
                user.put("ambiguities", amb);

                FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
                if (focusPhrase.isSuggestion()) {// 出入不完整
                    SuggestionResponse response = new SuggestionResponse(search);
                    SuggestionDatas datas = new SuggestionDatas();
                    JSONObject json = SuggestionBuild.sug(tokens, focusInst);
                    logger.debug("Get Suggestions:" + json);
                    datas.beginPos = json.getInteger("position");
                    datas.phraseBeginPos = datas.beginPos;
                    List<FocusNode> focusNodes = JSONArray.parseArray(json.getJSONArray("suggestions").toJSONString(), FocusNode.class);
                    focusNodes.forEach(node -> datas.suggestions.addAll(SuggestionBuild.buildSug(fp, user, node)));
                    response.setDatas(datas);

                    // search suggestions
                    session.sendMessage(new TextMessage(response.response()));

                    // search finish
                    session.sendMessage(new TextMessage(SearchFinishedResponse.response(search, received)));
                    logger.info("提示:\n\t" + JSON.toJSONString(focusNodes) + "\n");
                } else {//  输入完整

                    if (!isQuestion) {// formula
                        FormulaResponse response = new FormulaResponse(search);
                        FormulaAnalysis.FormulaObj formulaObj = FormulaAnalysis.analysis(focusInst.lastFocusPhrase());
                        FormulaDatas datas = new FormulaDatas();
                        datas.settings = FormulaAnalysis.getSettings(formulaObj);
                        datas.formulaObj = formulaObj.toString();
                        response.setDatas(datas);
                        session.sendMessage(new TextMessage(response.response()));
                        session.sendMessage(new TextMessage(SearchFinishedResponse.response(search, received)));
                        return;
                    }

                    StateResponse response = new StateResponse(search);

                    // 获取日期列
                    List<Column> dateColumns = SuggestionBuild.colRandomSuggestions(user, Constant.DataType.TIMESTAMP);
                    // 生成指令
                    JSONObject json = InstructionBuild.build(focusInst, search, amb, getFormula(user), language, dateColumns);

                    json.put("source", "searchUser"); // 区分是search框还是pinboard
                    json.put("sourceToken", user.getString("sourceToken"));

                    logger.info("指令:\n\t" + json + "\n");
                    // Annotations
                    AnnotationResponse annotationResponse = new AnnotationResponse(search);
                    annotationResponse.datas.addAll(getAnnotationDatas(json.getJSONArray("instructions")));
                    session.sendMessage(new TextMessage(annotationResponse.response()));

                    // TODO: 2018/5/11  add suggestion here

                    // search finish
                    session.sendMessage(new TextMessage(SearchFinishedResponse.response(search, received)));

                    if (!Constant.Event.TEXT_CHANGE.equalsIgnoreCase(event)) {
                        return;
                    }
                    // prepareQuery
                    response.setDatas("prepareQuery");
                    session.sendMessage(new TextMessage(response.response()));

                    // 指令检测
                    response.setDatas("precheck");
                    session.sendMessage(new TextMessage(response.response()));

                    if (checkQuery(session, json, search)) {
                        return;
                    }

                    // 指令检测完毕
                    response.setDatas("precheckDone");
                    session.sendMessage(new TextMessage(response.response()));

                    // 准备执行指令
                    response.setDatas("executeQuery");
                    session.sendMessage(new TextMessage(response.response()));

                    JSONObject res = Clients.Bi.query(json.toJSONString());
                    logger.debug("executeQuery result:" + res);
                    String taskId = res.getString("taskId");
                    session.getAttributes().put("taskId", taskId);
                    QuartzManager.addJob(taskId, session);

                    // 添加到历史记录中,并且放弃上一次搜索
                    addQuestion(new HistoryQuestion(search, json, taskId), user);

                }
            } else {//  出错
                IllegalResponse response = new IllegalResponse(search);
                int strPosition = tokens.get(focusInst.position).getStart();
                IllegalDatas datas = new IllegalDatas();
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
                logger.info(msg);
                msg = "提示:\n\t" + reason + "\n";
                logger.info(msg);
            }

        } catch (AmbiguitiesException e) {
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
            datas.title = "ambiguity word: " + title;
            e.ars.forEach(a -> datas.possibleMenus.add(a.columnName + " in table " + a.sourceName));
            response.setDatas(datas);
            session.sendMessage(new TextMessage(response.response()));
            logger.info(response.response());

            session.sendMessage(new TextMessage(SearchFinishedResponse.response(search, received)));

        } catch (IllegalException e) {
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

    /**
     * 记录到历史记录中,并且放弃上一次还未执行完的搜索
     *
     * @param current question, instruction, taskId
     * @param user    websocket 用户信息
     */
    private static void addQuestion(HistoryQuestion current, JSONObject user) throws FocusHttpException {
        JSONArray questions = user.getJSONArray("historyQuestions");
        if (questions.size() > 0) {
            HistoryQuestion last = (HistoryQuestion) questions.get(0);
            String taskId = HistoryQuestion.equals(last, current);
            if (taskId != null) {
                questions.add(0, current);
                user.put("historyQuestions", questions);
                Clients.Bi.abortQuery(taskId);
            }
        }
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
                IllegalDatas datas = new IllegalDatas(0, question.length() - 1, "Bi not support.");
                IllegalResponse illegal = new IllegalResponse(question, datas);
                session.sendMessage(new TextMessage(illegal.response()));
                return true;
            }
        } catch (FocusHttpException e) {
            IllegalDatas datas = new IllegalDatas(0, question.length() - 1, checkQuery.getString("exception"));
            IllegalResponse illegal = new IllegalResponse(question, datas);
            session.sendMessage(new TextMessage(illegal.response()));
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
            instName.append(fn.getValue());
            instName.append(space(fn.getBegin() - space));
            space = fn.getEnd();
        }
        return instName.toString();
    }

    private static String space(int count) {
        StringBuilder space = new StringBuilder("");
        int i = 0;
        while (i++ < count) {
            space.append(" ");
        }
        return space.toString();
    }


}
