package focus.search.controller;

import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusParser;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.controller.common.Base;
import focus.search.controller.common.SuggestionBuild;
import focus.search.instruction.InstructionBuild;
import focus.search.response.api.GetInstsResponse;
import focus.search.response.api.NameCheckResponse;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusHttpException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.FocusParserException;
import focus.search.response.search.ChartsResponse;
import focus.search.response.search.ErrorResponse;
import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class WebsocketSearch extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger(WebsocketSearch.class);

    public static final String RECEIVED_TIMESTAMP = "RECEIVED_TIMESTAMP";

    private static final ArrayList<WebSocketSession> users = new ArrayList<>();

    private JSONObject userInfo(String accessToken) throws FocusHttpException {
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

    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        if (users.size() >= Base.WebsocketLimit) {
            String warn = "Websocket connected too much.";
            logger.warn(warn);
            session.sendMessage(new TextMessage(warn));
            if (session.isOpen())
                session.close();
            return;
        }
        String accessToken = session.getAttributes().get("accessToken").toString();
        logger.info("current accessToken:" + accessToken);
        JSONObject user;
        try {
            // 获取用户信息
            user = userInfo(accessToken);
            user.put("accessToken", accessToken);
        } catch (FocusHttpException e) {
            logger.error(Common.printStacktrace(e));
            String warn = ErrorResponse.response(Constant.ErrorType.ERROR, "Get Userinfo fail.").toJSONString();
            session.sendMessage(new TextMessage(warn));
            session.close();
            return;
        }
        session.getAttributes().put("user", user);
        logger.info(user.getString("name") + " connected to server.");
        users.add(session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        JSONObject user = (JSONObject) session.getAttributes().getOrDefault("user", new JSONObject());
        users.remove(session);
        logger.info(user.getString("name") + " disconnected to server.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        session.getAttributes().put(RECEIVED_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
        String input = message.getPayload();

        // 每次通信前检测用户是否还处于登录状态
        String accessToken = session.getAttributes().get("accessToken").toString();
        try {
            if (!Base.isLogin(accessToken)) {
                FocusExceptionHandler.handle(session, ErrorResponse.response(Constant.ErrorType.NOT_LOGIN).toJSONString());
                session.close();
                return;
            }
        } catch (FocusHttpException e) {
            logger.error(Common.printStacktrace(e));
            FocusExceptionHandler.handle(session, ErrorResponse.response(Constant.ErrorType.NOT_LOGIN).toJSONString());
            return;
        }

        try {
            SearchHandler.preHandle(session, JSONObject.parseObject(input));
        } catch (IOException | FocusHttpException | FocusInstructionException | FocusParserException e) {
            logger.error(Common.printStacktrace(e));
            FocusExceptionHandler.handle(session, e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.info("get an error:" + exception.getMessage());
        session.sendMessage(new TextMessage(exception.getMessage()));
    }

    public static void queryResult(ChartsResponse chartsResponse, String taskId) throws IOException {
        for (WebSocketSession session : users) {
            if (session.getAttributes().get("taskId").toString().equalsIgnoreCase(taskId)) {
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
            // TODO: 2018/5/21 qiyi
            List<FocusToken> tokens = null;
            try {
                tokens = fp.focusAnalyzer.test(question, language);
            } catch (AmbiguitiesException e) {
                e.printStackTrace();
            }
            JSONObject amb = user.getJSONObject("ambiguities");

            try {
                // 解析结果
                FocusInst focusInst = fp.parseQuestion(tokens, amb);
                if (focusInst.position < 0) {
                    FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
                    if (!focusPhrase.isSuggestion()) {
                        JSONObject json = InstructionBuild.build(focusInst, question, amb, Base.getFormula(user));
                        response = new GetInstsResponse(Constant.Status.SUCCESS);
                        response.instructions = json.getString("instructions");
                    }
                }
            } catch (AmbiguitiesException | FocusParserException | FocusInstructionException e) {
                logger.error(Common.printStacktrace(e));
            }
            break;
        }
        response.cost = Calendar.getInstance().getTimeInMillis() - startTime;
        return response.toJSON();
    }

    public static JSONObject modelNameCheck(String name, String language, String type) {
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
