package focus.search.controller;

import focus.search.base.LoggerHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

class FocusExceptionHandler {

    static void handle(WebSocketSession session, Exception e) throws IOException {
        LoggerHandler.error(FocusExceptionHandler.stackTraceToStr(e));
        // todo 构造错误信息返回结构
        session.sendMessage(new TextMessage(e.getMessage()));
    }

    private static String stackTraceToStr(Exception e) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        sw.close();
        pw.close();
        return sw.toString();
    }
}
