package focus.search.controller;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.LoggerHandler;
import focus.search.bnf.exception.InvalidRuleException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;

public class WebsocketSearch extends TextWebSocketHandler {
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

}
