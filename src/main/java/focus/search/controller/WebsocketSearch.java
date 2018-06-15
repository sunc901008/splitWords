package focus.search.controller;

import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusParser;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.controller.common.Base;
import focus.search.controller.common.SuggestionBuild;
import focus.search.instruction.InstructionBuild;
import focus.search.response.api.GetInstsResponse;
import focus.search.response.api.NameCheckResponse;
import focus.search.response.exception.*;
import focus.search.response.search.ChartsResponse;
import focus.search.response.search.ErrorResponse;
import focus.search.suggestions.HistoryUtils;
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

    private static final List<WebSocketSession> users = new ArrayList<>();

    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        Base.afterConnectionEstablished(users, session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        JSONObject user = (JSONObject) session.getAttributes().getOrDefault("user", new JSONObject());
        // 断连接的时候持久化历史记录
        HistoryUtils.persistentHistory(user);
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
        } catch (IOException | FocusHttpException | FocusInstructionException | FocusParserException | IllegalException e) {
            logger.error(Common.printStacktrace(e));
            FocusExceptionHandler.handle(session, e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.info("get an error:" + exception.getMessage());
        JSONObject user = (JSONObject) session.getAttributes().getOrDefault("user", new JSONObject());
        // 断连接的时候持久化历史记录
        HistoryUtils.persistentHistory(user);
        if (session.isOpen())
            Common.send(session, exception.getMessage());
    }

    public static void queryResult(ChartsResponse chartsResponse, String taskId) throws IOException {
        for (WebSocketSession session : users) {
            if (session.getAttributes().get("taskId").toString().equalsIgnoreCase(taskId)) {
                Common.send(session, chartsResponse.response());
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
            logger.info("current user:" + user);
            FocusParser fp = (FocusParser) user.get("parser");
            String language = user.getString("language");

            // 分词
            // TODO: 2018/5/21 歧义
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
                    if (focusInst.isInstruction) {
                        JSONObject json = InstructionBuild.build(focusInst, question, amb, Base.getFormula(user), language);
                        response = new GetInstsResponse(Constant.Status.SUCCESS);
                        response.instructions = json.getString("instructions");
                    }
                }
            } catch (AmbiguitiesException | FocusInstructionException | IllegalException e) {
                logger.error(Common.printStacktrace(e));
            }
            break;
        }
        response.cost = Calendar.getInstance().getTimeInMillis() - startTime;
        return response.toJSON();
    }

    public static JSONObject modelNameCheck(String name, String language, String type) {
        FocusParser fp = Constant.Language.ENGLISH.equals(language) ? Base.englishParser.deepClone() : Base.chineseParser.deepClone();

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
