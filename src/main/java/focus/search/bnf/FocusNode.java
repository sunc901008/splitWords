package focus.search.bnf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.meta.Column;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusNode {

    private String value;

    private boolean isTerminal;

    private String type;

    private Integer begin;
    private Integer end;

    private Column column;

    private boolean hasChild = false;

    private FocusPhrase children;

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

    public void setTerminal() {
        isTerminal = true;
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

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public FocusPhrase getChildren() {
        return children;
    }

    public void setChildren(FocusPhrase children) {
        this.children = children;
        this.hasChild = true;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("realValue", value);
        json.put("isTerminal", isTerminal);
        json.put("type", type);
        if (column != null) {
            json.put("column", JSON.toJSON(column));
        }
        json.put("begin", begin);
        json.put("end", end);
        json.put("hasChild", hasChild);
        if (hasChild) {
            json.put("children", children.toJSON());
        }
        return json;
    }
}
