package focus.search.controller.api;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Common;
import focus.search.controller.WebsocketSearch;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.Calendar;

@Controller
@RequestMapping("/api/search")
public class Webservice {
    private static final Logger logger = Logger.getLogger(Webservice.class);

    @ResponseBody
    @RequestMapping(value = "/queryResult", method = RequestMethod.POST)
    public JSONObject queryResult(@RequestBody String data) throws IOException {
        logger.info(Common.cut(data));
        WebsocketSearch.queryResult(data);
        JSONObject response = new JSONObject();
        response.put("success", true);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/getInsts", method = RequestMethod.GET)
    public JSONObject getInsts(ServletRequest request) throws IOException {
        long startTime = Calendar.getInstance().getTimeInMillis();
        String sourceToken = request.getParameter("sourceToken");
        String question = request.getParameter("question");
        return WebsocketSearch.getInsts(sourceToken, question, startTime);
    }

    @ResponseBody
    @RequestMapping(value = "/modelNameCheck", method = RequestMethod.GET)
    public JSONObject modelNameCheck(ServletRequest request) throws IOException {
        String name = request.getParameter("name");
        String language = request.getParameter("language");
        String type = request.getParameter("type");
        return WebsocketSearch.modelNameCheck(name, language, type);
    }


}
