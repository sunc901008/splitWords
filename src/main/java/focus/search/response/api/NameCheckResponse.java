package focus.search.response.api;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;

/**
 * creator: sunc
 * date: 2018/5/4
 * description:
 */
public class NameCheckResponse {

    private String status;
    public String message;

    public NameCheckResponse(String status) {
        this.status = status;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("message", message);
        return json;
    }

    public static JSONObject response() {
        JSONObject json = new JSONObject();
        json.put("status", Constant.Status.SUCCESS);
        json.put("message", "legal");
        return json;
    }

}
