package focus.search.controller.api;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.LoggerHandler;
import focus.search.controller.WebsocketSearch;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/api/search")
public class Webservice {

    @ResponseBody
    @RequestMapping(value = "/queryResult", method = RequestMethod.POST)
    public JSONObject post(@RequestBody String data) throws IOException {
        LoggerHandler.info(data);
        WebsocketSearch.queryResult(data);
        JSONObject response = new JSONObject();
        response.put("success", true);
        return response;
    }

}

