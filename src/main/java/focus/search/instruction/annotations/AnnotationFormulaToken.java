package focus.search.instruction.annotations;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/24
 * description:
 */
public class AnnotationFormulaToken {
    public String description;
    public JSONObject formula;
    public String type;
    public String detailType;
    public String value;
    public Integer begin;
    public Integer end;
    public List<String> tokens = new ArrayList<>();

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("description", this.description);
        json.put("formula", this.formula);
        json.put("type", this.type);
        json.put("detailType", this.detailType);
        json.put("value", this.value);
        json.put("begin", this.begin);
        json.put("end", this.end);
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(this.tokens);
        if (!jsonArray.isEmpty())
            json.put("tokens", jsonArray);
        return json;
    }
}
