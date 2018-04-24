package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class IllegalResponse {

    private String question;
    private IllegalDatas datas;

    public IllegalResponse(String question) {
        this.question = question;
    }

    public IllegalResponse(String question, IllegalDatas datas) {
        this.question = question;
        this.datas = datas;
    }

    public IllegalDatas getDatas() {
        return datas;
    }

    public void setDatas(IllegalDatas datas) {
        this.datas = datas;
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "illegal");
        json.put("question", question);
        json.put("datas", datas.toJSON());
        return json.toJSONString();
    }

}
