package focus.search.controller;

import focus.search.controller.common.Base;
import org.apache.log4j.Logger;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Arrays;
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

        String accessToken = null;
        if (tokens != null && tokens.size() > 0) {
            List<String> cookies = Arrays.asList(tokens.get(0).split(";"));
            for (String cookie : cookies) {
                cookie = cookie.trim();
                if (cookie.startsWith("access_token")) {
                    accessToken = cookie.split("=")[1];
                    break;
                }
            }
        }
        // check user login
        attributes.put("accessToken", accessToken);
        return Base.isLogin(accessToken);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
    }

}
