package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/3/22
 * description:
 */
public class FormulaResponse {
    private String question;

    private FormulaDatas datas;

    public void setDatas(FormulaDatas datas) {
        this.datas = datas;
    }

    public FormulaResponse(String question) {
        this.question = question;
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "formula");
        json.put("question", question);
        json.put("datas", datas.toJSON());
        return json.toJSONString();
    }

}
