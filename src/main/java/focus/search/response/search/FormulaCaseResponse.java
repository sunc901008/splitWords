package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/3/23
 * description:
 */
public class FormulaCaseResponse {
    private JSONObject datas;

    public JSONObject getDatas() {
        return datas;
    }

    public void setDatas(JSONObject datas) {
        this.datas = datas;
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "response");
        json.put("command", "formulaCase");
        json.put("datas", datas);
        return json.toJSONString();
    }

}
