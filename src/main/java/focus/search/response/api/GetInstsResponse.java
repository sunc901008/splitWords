package focus.search.response.api;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/5/4
 * description:
 */
public class GetInstsResponse {

    private String status;
    public String instructions;
    public long cost;

    public GetInstsResponse(String status) {
        this.status = status;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("status", status);
        json.put("instructions", instructions);
        json.put("cost", cost);
        return json;
    }

}
