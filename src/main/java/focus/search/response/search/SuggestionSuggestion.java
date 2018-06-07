package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class SuggestionSuggestion {

    public String suggestion;
    public String suggestionType;
    public String description;
    public int beginPos;
    public int endPos;

    JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("suggestion", this.suggestion);
        json.put("suggestionType", this.suggestionType);
        json.put("description", this.description);
        json.put("beginPos", this.beginPos);
        json.put("endPos", this.endPos);
        return json;
    }

}
