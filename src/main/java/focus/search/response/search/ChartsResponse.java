package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class ChartsResponse {

    private String question;
    private String sourceToken;
    private JSONObject datas;

    public ChartsResponse(String question, String sourceToken) {
        this.question = question;
        this.sourceToken = sourceToken;
    }

    public ChartsResponse(String question) {
        this.question = question;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public void setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
    }

    public JSONObject getDatas() {
        return datas;
    }

    public void setDatas(JSONObject datas) {
        this.datas = datas;
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "charts");
        json.put("question", this.question);
        json.put("sourceToken", this.sourceToken);
        json.put("datas", this.datas);
        return json.toJSONString();
    }
}
