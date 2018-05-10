package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/5/10
 * description:
 */
public class FatalResponse {

    public static String response(String msg, String sourceToken) {
        JSONObject json = new JSONObject();
        json.put("type", "fatal");
        json.put("sourceToken", sourceToken);
        json.put("message", msg);
        json.put("status", "exception");
        return json.toJSONString();
    }

}
