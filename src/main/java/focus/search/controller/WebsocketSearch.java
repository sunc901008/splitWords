package focus.search.controller;

import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusParser;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.controller.common.Base;
import focus.search.controller.common.SuggestionBuild;
import focus.search.instruction.InstructionBuild;
import focus.search.response.api.GetInstsResponse;
import focus.search.response.api.NameCheckResponse;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.search.ChartsResponse;
import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WebsocketSearch extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger(WebsocketSearch.class);

    public static final String RECEIVED_TIMESTAMP = "RECEIVED_TIMESTAMP";

    private static final ArrayList<WebSocketSession> users = new ArrayList<>();
    private static final Integer WebsocketLimit = 10;

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        if (users.size() >= WebsocketLimit) {
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
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, InvalidRuleException {
        session.getAttributes().put(RECEIVED_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
        String input = message.getPayload();
        SearchHandler.preHandle(session, JSONObject.parseObject(input));
    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        logger.info("get an error:" + exception.getMessage());
//        session.sendMessage(new TextMessage(exception.getMessage()));
//    }

    public static void queryResult(String data) throws IOException {
        JSONObject json = JSONObject.parseObject(data);
        String taskId = json.getString("taskId");
        for (WebSocketSession session : users) {
            if (session.getAttributes().get("taskId").toString().equalsIgnoreCase(taskId)) {
                ChartsResponse chartsResponse = new ChartsResponse(json.getString("question"), json.getString("sourceToken"));
                chartsResponse.setDatas(json);
                session.sendMessage(new TextMessage(chartsResponse.response()));
                break;
            }
        }
    }

    public static JSONObject getInsts(String sourceToken, String question, long startTime) throws IOException {
        GetInstsResponse response = new GetInstsResponse(Constant.Status.ERROR);
        for (WebSocketSession session : users) {
            JSONObject user = (JSONObject) session.getAttributes().get("user");
            if (!sourceToken.equals(user.getString("sourceToken"))) {
                continue;
            }
            FocusParser fp = (FocusParser) user.get("parser");
            String language = user.getString("language");

            // 分词
            List<FocusToken> tokens = fp.focusAnalyzer.test(question, language);
            JSONObject amb = user.getJSONObject("ambiguities");

            try {
                // 解析结果
                FocusInst focusInst = fp.parseQuestion(tokens, amb);
                if (focusInst.position < 0) {
                    FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
                    if (!focusPhrase.isSuggestion()) {
                        JSONObject json = InstructionBuild.build(focusInst, question, amb, SearchHandler.getFormula(user));
                        response = new GetInstsResponse(Constant.Status.SUCCESS);
                        response.instructions = json.getString("instructions");
                    }
                }
            } catch (AmbiguitiesException | InvalidRuleException e) {
                logger.error(Common.printStacktrace(e));
            }
            break;
        }
        response.cost = Calendar.getInstance().getTimeInMillis() - startTime;
        return response.toJSON();
    }

    public static JSONObject modelNameCheck(String name, String language, String type) throws IOException {
        FocusParser fp = null;
        for (WebSocketSession session : users) {
            JSONObject user = (JSONObject) session.getAttributes().get("user");
            if (user.getString("language").equals(language)) {
                fp = (FocusParser) user.get("parser");
                break;
            }
        }
        if (fp == null) {
            fp = Constant.Language.ENGLISH.equals(language) ? Base.englishParser.deepClone() : Base.chineseParser.deepClone();
        }

        NameCheckResponse response = new NameCheckResponse(Constant.Status.ERROR);
        List<TerminalToken> tokens = SuggestionBuild.terminalTokens(fp, "<symbol>");
        for (TerminalToken token : tokens) {
            if (name.toLowerCase().contains(token.getName())) {
                response.message = token.getName() + " is invalid character(s)";
                return response.toJSON();
            }
        }

        for (TerminalToken token : fp.getTerminalTokens()) {
            if (name.toLowerCase().equals(token.getName().toLowerCase())) {
                response.message = token.getName() + " is a focus keyword";
                return response.toJSON();
            }
        }

        //  纯数字
        if (Common.intCheck(name) || Common.doubleCheck(name)) {
            response.message = name + " can not be a pure digital";
            return response.toJSON();
        }

        return NameCheckResponse.response();
    }

}
