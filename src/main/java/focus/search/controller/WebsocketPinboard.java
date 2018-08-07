package focus.search.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.base.LanguageUtils;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusParser;
import focus.search.bnf.ModelBuild;
import focus.search.controller.common.Base;
import focus.search.controller.common.QuartzManager;
import focus.search.instruction.InstructionBuild;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.metaReceived.Ambiguities;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusHttpException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.response.pinboard.InitStateResponse;
import focus.search.response.search.*;
import focus.search.suggestions.SourcesUtils;
import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class WebsocketPinboard extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger(WebsocketPinboard.class);

    private static final List<WebSocketSession> users = new ArrayList<>();

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Base.afterConnectionEstablished(users, session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        JSONObject user = (JSONObject) session.getAttributes().get("user");
        users.remove(session);
        logger.info(user.getString("name") + " disconnected to server.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject params = JSONObject.parseObject(message.getPayload());
        logger.info("user input:" + params);
        String type = params.getString("type");
        switch (type) {
            case "init":
                init(session, params.getJSONArray("answers"));
                break;
            case "query":
                Common.send(session, Response.response(type));
                JSONArray parsers = (JSONArray) session.getAttributes().get("parsers");
                if (parsers != null) {
                    query(session, params.getJSONObject("answers"), parsers);
                } else {
                    logger.error("parsers is null.");
                    ErrorResponse.response(Constant.ErrorType.ERROR, "parser is null");
                }
                break;
            case "echo":
            default:
                echo(session);
        }
    }

    private static void init(WebSocketSession session, JSONArray answers) throws IOException {
        Object object = session.getAttributes().get("user");
        JSONObject user = object == null ? new JSONObject() : (JSONObject) object;
        JSONArray parsers = new JSONArray();
        for (int i = 0; i < answers.size(); i++) {
            JSONObject pinboard = new JSONObject();
            JSONObject answer = answers.getJSONObject(i);
            String sourceToken = answer.getString("sourceToken");
            pinboard.put("sourceToken", sourceToken);
            JSONObject getSource;
            try {
                getSource = Clients.WebServer.getSource(sourceToken, user.getString("accessToken"));
            } catch (Exception e) {
                Common.send(session, ErrorResponse.response(Constant.ErrorType.ERROR, e.getMessage()).toJSONString());
                continue;
            }
            List<SourceReceived> srs;
            if ("success".equals(getSource.getString("status"))) {
                srs = JSONArray.parseArray(getSource.getJSONArray("sources").toJSONString(), SourceReceived.class);
            } else {
                continue;
            }
            pinboard.put("sources", srs);
            JSONObject context = answer.getJSONObject("context");
            String language = Constant.Language.ENGLISH;
            JSONObject ambiguities = new JSONObject();
            List<Formula> formulas = null;
            if (context != null) {
                ambiguities = Base.context(context, srs);
                language = context.getString("language");
                JSONArray formulaStr = context.getJSONArray("formulas");
                logger.info(String.format("formula:%s", formulaStr));
                if (formulaStr != null) {
                    formulas = JSONArray.parseArray(formulaStr.toJSONString(), Formula.class);
                    pinboard.put("formulas", formulas);
                }
            }
            FocusParser fp = Constant.Language.ENGLISH.equals(language) ? Base.englishParser.deepClone() : Base.chineseParser.deepClone();
            if (formulas != null) {
                ModelBuild.buildFormulas(fp, formulas);
            }
            pinboard.put("ambiguities", ambiguities);
            pinboard.put("language", language);
            ModelBuild.buildTable(fp, srs);
            pinboard.put("parser", fp);
            parsers.add(pinboard);
            InitStateResponse response = new InitStateResponse(sourceToken);
            Common.send(session, response.response());
        }
        session.getAttributes().put("parsers", parsers);
        Common.send(session, Response.response("init"));
    }

    private static void query(WebSocketSession session, JSONObject answer, JSONArray parsers) throws Exception {
        String sourceToken = answer.getString("sourceToken");
        String search = answer.getString("question");
        JSONObject biConfig = answer.getJSONObject("biConfig");
        List<Ambiguities> ambiguities = JSONArray.parseArray(answer.getString("ambiguities"), Ambiguities.class);
        for (int i = 0; i < parsers.size(); i++) {
            JSONObject pinboard = parsers.getJSONObject(i);
            if (sourceToken.equals(pinboard.getString("sourceToken"))) {
                JSONObject amb = pinboard.getJSONObject("ambiguities");
                @SuppressWarnings("unchecked")
                List<SourceReceived> srs = (List<SourceReceived>) pinboard.get("sources");
                Base.ambiguities(ambiguities, srs, amb);
                query(session, pinboard, search, amb, sourceToken, biConfig);
                break;
            }
        }
    }

    private static void query(WebSocketSession session, JSONObject pinboard, String search, JSONObject amb, String sourceToken, JSONObject biConfig) throws IOException {
        FocusParser fp = (FocusParser) pinboard.get("parser");
        String language = pinboard.getString("language");
        String ambiguityTitle = LanguageUtils.getMsg(language, LanguageUtils.Ambiguity_title);
        String ambiguityItem = LanguageUtils.getMsg(language, LanguageUtils.Ambiguity_item);
        @SuppressWarnings("unchecked")
        List<Formula> formulas = (List<Formula>) pinboard.get("formulas");
        List<FocusToken> tokens;
        FocusInst focusInst;
        try {
            tokens = fp.focusAnalyzer.test(search, language);
        } catch (AmbiguitiesException e) {
            logger.error(Common.printStacktrace(e));
            AmbiguityResponse response = new AmbiguityResponse(search);
            String ambiguityWord = search.substring(e.begin, e.end);
            String id = AmbiguitiesResolve.mergeAmbiguities(e.ars, ambiguityWord, amb);

            AmbiguityDatas datas = new AmbiguityDatas();
            datas.begin = e.begin;
            datas.end = e.end;
            datas.id = id;
            datas.title = String.format(ambiguityTitle, ambiguityWord);
            e.ars.forEach(a -> datas.possibleMenus.add(a.possibleValue));
            response.setDatas(datas);
            Common.send(session, response.response());
            logger.info(response.response());

            Common.send(session, SearchFinishedResponse.response(search, 0));
            return;
        }
        if (tokens.size() == 0) {
            IllegalResponse response = new IllegalResponse(search);
            Common.send(session, response.response());
            return;
        }
        logger.info("split words:" + JSON.toJSONString(tokens));

        @SuppressWarnings("unchecked")
        List<SourceReceived> srs = (List<SourceReceived>) pinboard.get("sources");
        JSONObject json;
        try {
            focusInst = fp.parseQuestion(tokens, amb, language, srs, formulas);
            if (!focusInst.isInstruction) {
                IllegalResponse response = new IllegalResponse(search);
                Common.send(session, response.response());
                return;
            }
            // 获取日期列
            List<Column> dateColumns = SourcesUtils.colRandomSuggestions(pinboard, Constant.DataType.TIMESTAMP);
            json = InstructionBuild.build(focusInst, search, amb, formulas, language, dateColumns);
        } catch (FocusInstructionException | IllegalException e) {
            logger.error(Common.printStacktrace(e));
            FocusExceptionHandler.handle(session, e);
            return;

        } catch (AmbiguitiesException e) {
            logger.error(Common.printStacktrace(e));
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

            Common.send(session, SearchFinishedResponse.response(search, 0));
            return;
        }

        StateResponse response = new StateResponse(search);
        // 生成指令
        response.setDatas("prepareQuery");
        // prepareQuery response
        Common.send(session, response.response());
        JSONArray instructions = json.getJSONArray("instructions");
        if (biConfig != null && !biConfig.isEmpty()) {
            JSONObject config = new JSONObject();
            config.put("instId", Constant.InstIdType.SET_BI_CONFIG);
            config.put("value", biConfig);
            instructions.add(config);
        }
        json.put("instructions", instructions.toJSONString());

        json.put("source", Constant.SearchOrPinboard.PINBOARD_USER);
        json.put("sourceToken", sourceToken);

        logger.info("指令:\n\t" + json + "\n");

        // Annotations
        AnnotationResponse annotationResponse = new AnnotationResponse(search);
        annotationResponse.datas.addAll(Base.getAnnotationDatas(instructions));
        // Annotations response
        Common.send(session, annotationResponse.response());

        // search finish response
        Common.send(session, SearchFinishedResponse.response(search));

        // 指令检测
        response.setDatas("precheck");
        // precheck response
        Common.send(session, response.response());

        try {
            if (Base.checkQuery(session, json, search, pinboard.getString("accessToken"))) {
                return;
            }
        } catch (IOException e) {
            logger.error(Common.printStacktrace(e));
        }

        // 指令检测完毕
        response.setDatas("precheckDone");
        // precheckDone response
        Common.send(session, response.response());

        // 准备执行指令
        response.setDatas("executeQuery");
        // executeQuery response
        Common.send(session, response.response());

        JSONObject res;
        try {
            res = Clients.WebServer.query(json.toJSONString(), pinboard.getString("accessToken"));
        } catch (FocusHttpException e) {
            logger.error(Common.printStacktrace(e));
            FocusExceptionHandler.handle(session, e);
            return;
        }
        String taskId = res.getString("taskId");

        QuartzManager.addJob(taskId, session);

    }

    private static void echo(WebSocketSession session) throws IOException {
        JSONObject response = new JSONObject();
        response.put("type", "echo");
        Common.send(session, response.toJSONString());
    }

    public static void queryResult(ChartsResponse chartsResponse, String taskId) throws IOException {
        for (WebSocketSession session : users) {
            if (session.getAttributes().get("taskId").toString().equalsIgnoreCase(taskId)) {
                Common.send(session, chartsResponse.response());
                break;
            }
        }
    }

}
