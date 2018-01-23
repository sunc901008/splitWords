package focus.search.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusKWDict;
import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;

public class WsTest extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger(WsTest.class);
    private static final ArrayList<WebSocketSession> users = new ArrayList<>();

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("someone connected to server.");
        users.add(session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        users.remove(session);
        logger.info("someone disconnected to server.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        JSONObject params = JSON.parseObject(message.getPayload());
        String type = params.getString("type");
        String language = params.getString("lang");
        if(language == null){
            language = "english";
        }
        String input = params.getString("input");
        Object msg = "success";
        switch (type) {
            case "init":
                FocusAnalyzer.init();
                break;
            case "table":
                FocusAnalyzer.addTable(params.getJSONObject("table"));
                break;
            case "reset":
                FocusAnalyzer.reset();
                break;
            case "keyword":
                msg = FocusKWDict.getAllKeywords();
                break;
            case "split":
                msg = FocusAnalyzer.test(input, language);
                break;
            default:
                msg = "error";
        }
        JSONObject json = new JSONObject();
        json.put("result", msg);
        if (input != null) {
            json.put("input", input);
        }
        TextMessage send = new TextMessage(json.toJSONString());
        session.sendMessage(send);
    }

}
