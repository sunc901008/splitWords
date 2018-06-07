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
public class IllegalDatas {
    public Integer beginPos;
    public Integer endPos;
    public String reason;
    public List<SuggestionSuggestion> suggestions = new ArrayList<>();

    public IllegalDatas() {
    }

    public IllegalDatas(int beginPos, int endPos, String reason) {
        this.beginPos = beginPos;
        this.endPos = endPos;
        this.reason = reason;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("beginPos", beginPos);
        json.put("endPos", endPos);
        json.put("reason", reason);
        JSONArray jsonArray = new JSONArray();
        this.suggestions.forEach(suggestion -> jsonArray.add(suggestion.toJSON()));
        json.put("suggestions", jsonArray);
        return json;
    }
}
