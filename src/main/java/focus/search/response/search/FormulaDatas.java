package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/4/24
 * description:
 */
public class FormulaDatas {

    public FormulaSettings settings;
    public String formulaObj;

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("settings", settings);
        json.put("formulaObj", formulaObj);
        return json;
    }
}
