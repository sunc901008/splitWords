package focus.search.response.search;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.meta.Formula;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class AnnotationResponse {
    public String question;
    public List<Datas> datas = new ArrayList<>();

    public AnnotationResponse(String question) {
        this.question = question;
    }

    public static class Datas {
        public String type;
        public Integer id;
        public String category;
        public Integer begin;
        public Integer end;
        public JSONArray tokens = new JSONArray();

        JSONObject toJSON() {
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

    public static class Tokens {
        public String description;
        public String columnName;
        public String tableName;
        public Integer columnId;
        public String type;
        public String detailType;
        public String value;
        public Integer begin;
        public Integer end;
        public List<String> tokens = new ArrayList<>();
        public AmbiguityResponse.Datas ambiguity;

        JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("description", this.description);
            json.put("columnName", this.columnName);
            json.put("tableName", this.tableName);
            json.put("columnId", this.columnId);
            json.put("type", this.type);
            json.put("detailType", this.detailType);
            json.put("value", this.value);
            json.put("begin", this.begin);
            json.put("columnId", this.columnId);
            json.put("end", this.end);
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(this.tokens);
            if (!jsonArray.isEmpty())
                json.put("tokens", jsonArray);
            return json;
        }
    }

    public static class FormulaTokens {
        public String description;
        public Formula formula;
        public String type;
        public String detailType;
        public String value;
        public Integer begin;
        public Integer end;
        public List<String> tokens = new ArrayList<>();

        JSONObject toJSON() {
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

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "annotations");
        json.put("question", this.question);
        JSONArray jsonArray = new JSONArray();
        this.datas.forEach(data -> jsonArray.add(data.toJSON()));
        json.put("datas", jsonArray);
        return json.toJSONString();
    }
}
