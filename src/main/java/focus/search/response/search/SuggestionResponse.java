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
public class SuggestionResponse {

    private String question;
    private Datas datas;

    public SuggestionResponse(String question) {
        this.question = question;
    }

    public Datas getDatas() {
        return datas;
    }

    public void setDatas(Datas datas) {
        this.datas = datas;
    }

    public static class Datas {
        public Integer beginPos;
        public Integer phraseBeginPos;
        public String guidance;
        public List<Suggestions> suggestions = new ArrayList<>();

        JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("beginPos", this.beginPos);
            json.put("phraseBeginPos", this.phraseBeginPos);
            json.put("guidance", this.guidance);
            JSONArray jsonArray = new JSONArray();
            this.suggestions.forEach(suggestion -> jsonArray.add(suggestion.toJSON()));
            json.put("tokens", jsonArray);
            return json;
        }

    }

    public static class Suggestions {
        public String suggestion;
        public String suggestionType;
        public String description;

        JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("suggestion", this.suggestion);
            json.put("suggestionType", this.suggestionType);
            json.put("description", this.description);
            return json;
        }

    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "suggestions");
        json.put("question", this.question);
        json.put("datas", this.datas.toJSON());
        return json.toJSONString();
    }

}
