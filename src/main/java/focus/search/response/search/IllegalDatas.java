package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/4/24
 * description:
 */
public class IllegalDatas {
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
