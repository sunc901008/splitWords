package focus.search.response.search;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class SuggestionResponse {

    private String question;
    private SuggestionDatas datas;

    public SuggestionResponse(String question) {
        this.question = question;
    }

    public SuggestionDatas getDatas() {
        return datas;
    }

    public void setDatas(SuggestionDatas datas) {
        List<SuggestionSuggestion> suggestions = datas.suggestions;
        suggestions = suggestions.subList(0, Math.min(SIZE, suggestions.size()));
        datas.suggestions = suggestions;
        this.datas = datas;
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "suggestions");
        json.put("question", this.question);
        json.put("datas", this.datas.toJSON());
        return json.toJSONString();
    }

    /**
     * 过滤suggestion,各类型的suggestion数量限制
     */
    public static final int SIZE = 10;

}
