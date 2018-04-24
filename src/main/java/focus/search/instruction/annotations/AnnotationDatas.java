package focus.search.instruction.annotations;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/4/24
 * description:
 */
public class AnnotationDatas {
    public String type;
    public Integer id;
    public String category;
    public Integer begin;
    public Integer end;
    public JSONArray tokens = new JSONArray();

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", this.type);
        json.put("id", this.id);
        json.put("category", this.category);
        json.put("begin", this.begin);
        json.put("end", this.end);
        json.put("tokens", this.tokens);
        return json;
    }
}
