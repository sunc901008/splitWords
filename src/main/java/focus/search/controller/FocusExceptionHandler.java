package focus.search.controller;

import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.response.exception.FocusHttpException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.FocusParserException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.ErrorResponse;
import focus.search.response.search.FatalResponse;
import focus.search.response.search.IllegalResponse;
import org.apache.log4j.Logger;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

class FocusExceptionHandler {
    private static final Logger logger = Logger.getLogger(FocusExceptionHandler.class);

    static void handle(WebSocketSession session, Exception e) throws IOException {
        logger.error(Common.printStacktrace(e));
        if (e instanceof IOException) {
            handle(session, ErrorResponse.response(e.getMessage()).toJSONString());
        } else if (e instanceof FocusHttpException) {
            handle(session, ErrorResponse.response(Constant.ErrorType.ERROR).toJSONString());
        } else if (e instanceof FocusInstructionException) {
            handle(session, FatalResponse.response(e.getMessage(), session.getAttributes().get("sourceToken").toString()));
        } else if (e instanceof FocusParserException) {
            handle(session, ErrorResponse.response(e.getMessage()).toJSONString());
        } else if (e instanceof IllegalException) {
            IllegalException exp = (IllegalException) e;
            IllegalResponse response = new IllegalResponse(exp.question, exp.datas);
            handle(session, response.response());
        } else {
            handle(session, e.getMessage());
        }
    }

    static void handle(WebSocketSession session, String textMessage) throws IOException {
        Common.send(session, textMessage);
    }
}
