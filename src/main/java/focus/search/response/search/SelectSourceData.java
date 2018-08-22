package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/8/22
 * description:
 */
public class SelectSourceData {
    public Integer id;
    public String name;

    public SelectSourceData() {
    }

    public SelectSourceData(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        return json;
    }
}
