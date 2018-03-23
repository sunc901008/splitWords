package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/3/22
 * description:
 */
public class FormulaResponse {
    private String question;

    public void setDatas(Datas datas) {
        this.datas = datas;
    }

    private Datas datas;

    public FormulaResponse(String question) {
        this.question = question;
    }

    public static class Datas {
        public Settings settings;
        public String formulaObj;

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("settings", settings);
            json.put("formulaObj", formulaObj);
            return json;
        }

    }

    public static class Settings {
        public String dataType;
        public List<String> columnType;
        public List<String> aggregation;

    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "formula");
        json.put("question", question);
        json.put("datas", datas.toJSON());
        return json.toJSONString();
    }

}
