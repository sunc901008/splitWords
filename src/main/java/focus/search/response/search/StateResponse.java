package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class StateResponse {

    private String question;
    private String datas;

    public StateResponse(String question) {
        this.question = question;
    }

    public StateResponse(String question, String datas) {
        this.question = question;
        this.datas = datas;
    }

    public String getDatas() {
        return datas;
    }

    public void setDatas(String datas) {
        this.datas = datas;
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "state");
        json.put("question", question);
        json.put("datas", datas);
        return json.toJSONString();
    }

}
