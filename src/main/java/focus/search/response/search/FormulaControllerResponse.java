package focus.search.response.search;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/3/23
 * description:
 */
public class FormulaControllerResponse {
    public JSONArray datas = new JSONArray();

    public String response(String command) {
        JSONObject json = new JSONObject();
        json.put("type", "response");
        json.put("command", command);
        json.put("datas", datas);
        return json.toJSONString();
    }

}
