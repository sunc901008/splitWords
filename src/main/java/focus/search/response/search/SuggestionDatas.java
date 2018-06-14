package focus.search.response.search;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        addSug(ss, false);
    }

    /**
     * @param ss      SuggestionSuggestion
     * @param hasSame 是否已经添加了相同的suggestion,如两个name列,描述改为2/n个匹配项
     */
    public void addSug(SuggestionSuggestion ss, boolean hasSame) {
        if (this.beginPos < 0) {
            this.beginPos = ss.beginPos;
        } else if (!Constant.SuggestionType.HISTORY.equals(ss.suggestionType) && this.beginPos > ss.beginPos) {
            this.beginPos = ss.beginPos;
        }
        if (hasSame) {
            for (SuggestionSuggestion suggestion : this.suggestions) {
                if (suggestion.suggestion.equals(ss.suggestion)) {
                    suggestionAddItem(suggestion);
                    return;
                }
            }
        } else {
            this.suggestions.add(ss);
        }
    }

    private void suggestionAddItem(SuggestionSuggestion suggestion) {
        Pattern pattern = Pattern.compile("^(\\d+)(.*)");
        Matcher matcher = pattern.matcher(suggestion.description);
        if (matcher.matches()) {
            suggestion.description = Integer.parseInt(matcher.group(1)) + 1 + matcher.group(2);
        } else {
            suggestion.description = "2 matching items";
        }
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
