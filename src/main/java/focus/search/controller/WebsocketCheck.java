package focus.search.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * creator: sunc
 * date: 2018/1/30
 * description:
 */
public class WebsocketCheck extends HttpSessionHandshakeInterceptor {
    private static final Logger logger = Logger.getLogger(WebsocketCheck.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        List<String> tokens = request.getHeaders().get("Cookie");
        List<String> protocols = request.getHeaders().get("Sec-WebSocket-Protocol");
        if (protocols != null && protocols.size() > 0) {
            logger.info("protocols:" + protocols + ". first protocol:" + protocols.get(0));
            response.getHeaders().set("Sec-WebSocket-Protocol", protocols.get(0));
        }

        String token = "null";
        if (tokens.size() > 0) {
            List<String> cookies = Arrays.asList(tokens.get(0).split(";"));
            for (String cookie : cookies) {
                cookie = cookie.trim();
                if (cookie.startsWith("access_token")) {
                    token = cookie.split("=")[1];
                    break;
                }
            }
        }
        // todo get user info from uc
        attributes.put("user", userInfo(token));

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
    }

    private JSONObject userInfo(String token) {
        JSONObject user = new JSONObject();
        int id = Integer.parseInt(String.valueOf(Math.random() * 10));
        user.put("id", id);
        user.put("access_token", token);
        user.put("name", "admin" + id);
        user.put("username", "admin" + id);
        user.put("privileges", Collections.singletonList("[\"ADMIN\"]"));
        return user;
    }

}
