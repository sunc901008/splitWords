package focus.search.controller;

import focus.search.base.Common;
import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

class FocusExceptionHandler {
    private static final Logger logger = Logger.getLogger(FocusExceptionHandler.class);

    static void handle(WebSocketSession session, Exception e) throws IOException {
        logger.error(Common.printStacktrace(e));
        // todo 构造错误信息返回结构
        session.sendMessage(new TextMessage(e.getMessage()));
    }
}
