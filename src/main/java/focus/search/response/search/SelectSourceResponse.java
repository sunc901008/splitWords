package focus.search.response.search;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/8/22
 * description: {type: "selectSource", datas: [{name: "各国人口收入与消费能力", id: 4}, {name: "adad", id: 15}]}
 */
public class SelectSourceResponse {
    public List<SelectSourceData> datas = new ArrayList<>();

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "selectSource");
        JSONArray jsonArray = new JSONArray();
        this.datas.forEach(data -> jsonArray.add(data.toJSON()));
        json.put("datas", datas);
        return json.toJSONString();
    }
}
