package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class IllegalResponse {

    private String question;
    private Datas datas;

    public IllegalResponse(String question) {
        this.question = question;
    }

    public IllegalResponse(String question, Datas datas) {
        this.question = question;
        this.datas = datas;
    }

    public Datas getDatas() {
        return datas;
    }

    public void setDatas(Datas datas) {
        this.datas = datas;
    }

    public static class Datas {
        public Integer beginPos;
        public Integer endPos;
        public String reason;

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("beginPos", beginPos);
            json.put("endPos", endPos);
            json.put("reason", reason);
            return json;
        }
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "illegal");
        json.put("question", question);
        json.put("datas", datas.toJSON());
        return json.toJSONString();
    }

}
