package focus.search.response.pinboard;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class InitStateResponse {

    public String sourceToken;

    public InitStateResponse(String sourceToken) {
        this.sourceToken = sourceToken;
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "state");
        json.put("sourceToken", this.sourceToken);
        json.put("message", "initSucc");
        return json.toJSONString();
    }
}
