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
import focus.search.controller.common.Base;
import focus.search.controller.common.QuartzManager;
import focus.search.instruction.InstructionBuild;
import focus.search.meta.Formula;
import focus.search.metaReceived.Ambiguities;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.pinboard.InitStateResponse;
import focus.search.response.search.*;
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

    private static final ArrayList<WebSocketSession> users = new ArrayList<>();

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (users.size() >= Base.WebsocketLimit) {
            String warn = "Websocket connected too much.";
            logger.warn(warn);
            session.sendMessage(new TextMessage(warn));
            if (session.isOpen())
                session.close();
            return;
        }

        JSONObject user = (JSONObject) session.getAttributes().get("user");
        logger.info(user.getString("name") + " connected to server.");
        users.add(session);

    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        JSONObject user = (JSONObject) session.getAttributes().get("user");
        users.remove(session);
        logger.info(user.getString("name") + " disconnected to server.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject params = JSONObject.parseObject(message.getPayload());
        String type = params.getString("type");
        switch (type) {
            case "init":
                init(session, params.getJSONArray("answers"));
            case "query":
                session.sendMessage(new TextMessage(Response.response(type)));
                JSONArray parsers = (JSONArray) session.getAttributes().get("parsers");
                if (parsers != null)
                    query(session, params.getJSONObject("answers"), parsers);
            case "echo":
            default:
                echo(session);
        }
    }

    private static void init(WebSocketSession session, JSONArray answers) throws IOException {
        JSONArray parsers = new JSONArray();
        for (int i = 0; i < answers.size(); i++) {
            JSONObject pinboard = new JSONObject();
            JSONObject answer = answers.getJSONObject(i);
            String sourceToken = answer.getString("sourceToken");
            pinboard.put("sourceToken", sourceToken);
            JSONObject getSource;
            try {
                getSource = Clients.WebServer.getSource(sourceToken);
            } catch (Exception e) {
                session.sendMessage(new TextMessage(ErrorResponse.response(Constant.ErrorType.ERROR, e.getMessage()).toJSONString()));
                continue;
            }
            List<SourceReceived> srs;
            if ("success".equals(getSource.getString("status"))) {
                srs = JSONArray.parseArray(getSource.getJSONArray("sources").toJSONString(), SourceReceived.class);
            } else {
                continue;
            }
            pinboard.put("sources", srs);
            String context = answer.getString("context");
            String language = Constant.Language.ENGLISH;
            JSONObject ambiguities = new JSONObject();
            if (context != null) {
                JSONObject contextJson = JSONObject.parseObject(context);
                ambiguities = Base.context(contextJson, srs);
                language = contextJson.getString("language");
                String formulaStr = contextJson.getString("formulas");
                if (!Common.isEmpty(formulaStr)) {
                    List<Formula> formulas = JSONArray.parseArray(formulaStr, Formula.class);
                    pinboard.put("formulas", formulas);
                }
            }
            pinboard.put("ambiguities", ambiguities);
            pinboard.put("language", language);
            FocusParser fp = Constant.Language.ENGLISH.equals(language) ? Base.englishParser.deepClone() : Base.chineseParser.deepClone();
            ModelBuild.buildTable(fp, srs);
            pinboard.put("parser", fp);
            parsers.add(pinboard);
            InitStateResponse response = new InitStateResponse(sourceToken);
            session.sendMessage(new TextMessage(response.response()));
        }
        session.getAttributes().put("parsers", parsers);
        session.sendMessage(new TextMessage(Response.response("init")));
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
                query(session, pinboard, search, amb, sourceToken);
                break;
            }
        }
    }

    private static void query(WebSocketSession session, JSONObject pinboard, String search, JSONObject amb, String sourceToken) throws Exception {
        FocusParser fp = (FocusParser) pinboard.get("parser");
        String language = pinboard.getString("language");
        @SuppressWarnings("unchecked")
        List<Formula> formulas = (List<Formula>) pinboard.get("formulas");
        List<FocusToken> tokens = fp.focusAnalyzer.test(search, language);
        if (tokens.size() == 0) {
            // TODO: 2018/5/8 return error

            return;
        }
        FocusInst focusInst = fp.parseQuestion(tokens, amb);
        if (focusInst.position >= 0 || focusInst.lastFocusPhrase().isSuggestion()) {
            // TODO: 2018/5/8 return error

            return;
        }

        StateResponse response = new StateResponse(search);
        // 生成指令
        response.setDatas("prepareQuery");
        // prepareQuery response
        session.sendMessage(new TextMessage(response.response()));
        JSONObject json = InstructionBuild.build(focusInst, search, amb, formulas);

        json.put("source", "pinboardUser");
        json.put("sourceToken", sourceToken);

        logger.info("指令:\n\t" + json + "\n");

        // Annotations
        AnnotationResponse annotationResponse = new AnnotationResponse(search);
        annotationResponse.datas.addAll(Base.getAnnotationDatas(json.getJSONArray("instructions")));
        // Annotations response
        session.sendMessage(new TextMessage(annotationResponse.response()));

        // search finish response
        session.sendMessage(new TextMessage(SearchFinishedResponse.response(search)));

        // 指令检测
        response.setDatas("precheck");
        // precheck response
        session.sendMessage(new TextMessage(response.response()));

        if (Base.checkQuery(session, json, search)) {
            return;
        }

        // 指令检测完毕
        response.setDatas("precheckDone");
        // precheckDone response
        session.sendMessage(new TextMessage(response.response()));

        // 准备执行指令
        response.setDatas("executeQuery");
        // executeQuery response
        session.sendMessage(new TextMessage(response.response()));

        JSONObject res = Clients.WebServer.query(json.toJSONString());
        String taskId = res.getString("taskId");

        QuartzManager.addJob(taskId, session);

    }

    private static void echo(WebSocketSession session) throws IOException {
        JSONObject response = new JSONObject();
        response.put("type", "echo");
        session.sendMessage(new TextMessage(response.toJSONString()));
    }

    public static void queryResult(ChartsResponse chartsResponse, String taskId) throws IOException {
        for (WebSocketSession session : users) {
            if (session.getAttributes().get("taskId").toString().equalsIgnoreCase(taskId)) {
                session.sendMessage(new TextMessage(chartsResponse.response()));
                break;
            }
        }
    }

}
