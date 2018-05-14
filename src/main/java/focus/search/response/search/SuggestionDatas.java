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
public class SuggestionDatas {

    public Integer beginPos;
    public Integer phraseBeginPos;
    public String guidance;
    public List<SuggestionSuggestion> suggestions = new ArrayList<>();

    JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("beginPos", this.beginPos);
        json.put("phraseBeginPos", this.phraseBeginPos);
        json.put("guidance", this.guidance);
        JSONArray jsonArray = new JSONArray();
        this.suggestions.forEach(suggestion -> jsonArray.add(suggestion.toJSON()));
        json.put("suggestions", jsonArray);
        return json;
    }

}
