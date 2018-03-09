package focus.search.response.search;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class AmbiguityResponse {
    private String question;
    private Datas datas;

    public AmbiguityResponse(String question) {
        this.question = question;
    }

    public void setDatas(Datas datas) {
        this.datas = datas;
    }

    public static class Datas {
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

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "ambiguity");
        json.put("question", question);
        json.put("datas", datas);
        return json.toJSONString();
    }

}
