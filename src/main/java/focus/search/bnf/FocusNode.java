package focus.search.bnf;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusNode {

    private String value;

    private boolean isTerminal = false;

    private String type;

    private Integer begin;
    private Integer end;

    private List<FocusNodeDetail> details;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getBegin() {
        return begin;
    }

    public void setBegin(Integer begin) {
        this.begin = begin;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public List<FocusNodeDetail> getDetails() {
        return details;
    }

    public void addDetail(FocusNodeDetail detail) {
        if (details == null)
            details = new ArrayList<>();
        details.add(detail);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("value", value);
        json.put("isTerminal", isTerminal);
        json.put("type", type);
        json.put("begin", begin);
        json.put("end", end);
        return json;
    }
}
