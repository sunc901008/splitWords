package focus.search.response.search;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;

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

    public SuggestionDatas() {
        this.beginPos = -1;
    }

    /**
     * 添加suggestion,同时更新提示的起始位置
     *
     * @param ss SuggestionSuggestion
     */
    public void addSug(SuggestionSuggestion ss) {
        if (this.beginPos < 0) {
            this.beginPos = ss.beginPos;
        } else if (!Constant.SuggestionType.HISTORY.equals(ss.suggestionType) && this.beginPos > ss.beginPos) {
            this.beginPos = ss.beginPos;
        }
        for (SuggestionSuggestion suggestion : this.suggestions) {
            if (suggestion.suggestion.equals(ss.suggestion)) {
                return;
            }
        }
        this.suggestions.add(ss);
    }

    public void addAllSug(List<SuggestionSuggestion> sss) {
        sss.forEach(this::addSug);
    }

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
