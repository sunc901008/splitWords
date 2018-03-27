package focus.search.response.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class ExportContextResponse {

    public static String response(JSONObject context) {
        JSONObject json = new JSONObject();
        json.put("type", "response");
        json.put("command", "exportContext");
        JSONObject datas = new JSONObject();
        datas.put("status", "success");
        datas.put("message", "done");
        datas.put("context", context);
        json.put("datas", datas);
        return JSON.toJSONString(json, SerializerFeature.WriteMapNullValue);
    }

}
