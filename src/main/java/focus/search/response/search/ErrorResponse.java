package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class ErrorResponse {

    public static JSONObject response(String status) {
        JSONObject json = new JSONObject();
        json.put("type", "error");
        json.put("status", status);
        return json;
    }

    public static JSONObject response(String status, String msg) {
        JSONObject json = new JSONObject();
        json.put("type", "error");
        json.put("status", status);
        json.put("message", msg);
        return json;
    }

}
