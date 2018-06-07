package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

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
     * 过滤suggestion,各类型的suggestion数量限制,以及去重
     */
    // TODO: 2018/6/6
    public void filterDatas() {

    }

}
