package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class AmbiguityResponse {
    private String question;
    private AmbiguityDatas datas;

    public AmbiguityResponse(String question) {
        this.question = question;
    }

    public void setDatas(AmbiguityDatas datas) {
        this.datas = datas;
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "ambiguity");
        json.put("question", question);
        json.put("datas", datas);
        return json.toJSONString();
    }

}
