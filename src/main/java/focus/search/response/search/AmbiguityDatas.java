package focus.search.response.search;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/24
 * description:
 */
public class AmbiguityDatas {
    public String title;
    public String id;
    public Integer begin;
    public Integer end;
    public List<String> possibleMenus = new ArrayList<>();

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("title", title);
        json.put("id", id);
        json.put("begin", begin);
        json.put("end", end);
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(this.possibleMenus);
        json.put("possibleMenus", jsonArray);
        return json;
    }
}
