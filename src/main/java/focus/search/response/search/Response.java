package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class Response {

    public static String response(String command) {
        JSONObject json = new JSONObject();
        json.put("type", "response");
        json.put("command", command);
        JSONObject datas = new JSONObject();
        datas.put("status", "success");
        datas.put("message", "done");
        json.put("datas", datas);
        return json.toJSONString();
    }

}
