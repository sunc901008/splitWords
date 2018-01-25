package focus.search.bnf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusNode {

    private String value;

    private boolean isTerminal = false;

    private FocusToken ft;

    public FocusNode() {
    }

    public FocusNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public void setTerminal(boolean terminal) {
        isTerminal = terminal;
    }

    public FocusToken getFt() {
        return ft;
    }

    public void setFt(FocusToken ft) {
        this.ft = ft;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("value", value);
        json.put("isTerminal", isTerminal);
        json.put("FocusToken", JSON.parseObject(JSON.toJSONString(ft)));
        return json;
    }
}
