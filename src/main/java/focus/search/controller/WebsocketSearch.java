package focus.search.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.DefaultModel;
import focus.search.bnf.exception.InvalidRuleException;
import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;

public class WebsocketSearch extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger(WebsocketSearch.class);
    private static final ArrayList<WebSocketSession> users = new ArrayList<>();
    private static final Integer WebsocketLimit = 10;

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        if (users.size() >= WebsocketLimit) {
            String warn = "Websocket connected is too much.";
            logger.warn(warn);
            session.sendMessage(new TextMessage(warn));
            if (session.isOpen())
                session.close();
            return;
        }

        JSONObject user = (JSONObject) session.getAttributes().get("user");
        logger.info(user.getString("name") + " connected to server.");
        users.add(session);

//        List<TerminalToken> terminals = FocusParser.getTerminalTokens();
//        String msg = "最小单元词:\n\t" + JSON.toJSONString(terminals) + "\n";
//        System.out.println(msg);
//        session.sendMessage(new TextMessage(msg));

    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        JSONObject user = (JSONObject) session.getAttributes().get("user");
        users.remove(session);
        logger.info(user.getString("name") + " disconnected to server.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, InvalidRuleException {
        String input = message.getPayload();
        session.sendMessage(new TextMessage("your input:" + input));

        if ("sources".equals(input)) {
            session.sendMessage(new TextMessage(JSON.toJSONString(DefaultModel.sources())));
            return;
        }
        SearchHandler.preHandle(session, JSONObject.parseObject(input));
    }

}
