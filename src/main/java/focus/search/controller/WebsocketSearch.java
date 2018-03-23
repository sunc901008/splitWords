package focus.search.controller;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.LoggerHandler;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.response.search.ChartsResponse;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class WebsocketSearch extends TextWebSocketHandler {
    public static final String RECEIVED_TIMESTAMP = "RECEIVED_TIMESTAMP";

    private static final ArrayList<WebSocketSession> users = new ArrayList<>();
    private static final Integer WebsocketLimit = 10;

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        if (users.size() >= WebsocketLimit) {
            String warn = "Websocket connected too much.";
            LoggerHandler.warn(warn);
            session.sendMessage(new TextMessage(warn));
            if (session.isOpen())
                session.close();
            return;
        }

        JSONObject user = (JSONObject) session.getAttributes().get("user");
        LoggerHandler.info(user.getString("name") + " connected to server.");
        users.add(session);

    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        JSONObject user = (JSONObject) session.getAttributes().get("user");
        users.remove(session);
        LoggerHandler.info(user.getString("name") + " disconnected to server.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, InvalidRuleException {
        session.getAttributes().put(RECEIVED_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
        String input = message.getPayload();
        session.sendMessage(new TextMessage("your input:" + input));
        SearchHandler.preHandle(session, JSONObject.parseObject(input));
    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        LoggerHandler.info("get an error:" + exception.getMessage());
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

}
