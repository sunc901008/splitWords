package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class ExceptionResponse {

    public static JSONObject response(String msg) {
        JSONObject json = new JSONObject();
        json.put("type", "error");
        json.put("message", msg);
        return json;
    }
}
