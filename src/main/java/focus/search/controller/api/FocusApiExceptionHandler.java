package focus.search.controller.api;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Common;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusParserException;
import focus.search.response.search.ErrorResponse;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@ControllerAdvice
public class FocusApiExceptionHandler {
    private static final Logger logger = Logger.getLogger(FocusApiExceptionHandler.class);

    @ExceptionHandler({IOException.class, AmbiguitiesException.class, InvalidRuleException.class, FocusParserException.class, SchedulerException.class})
    @ResponseBody
    public JSONObject exception(IOException e) {
        logger.error(Common.printStacktrace(e));
        return ErrorResponse.response(e.getMessage());
    }

}
