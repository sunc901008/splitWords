package focus.search.bnf;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusNode {

    private List<FocusNode> children = new ArrayList<>();

    private String value;

    private String type;

    public FocusNode() {
    }

    public FocusNode(String value) {
        this.value = value;
    }

    public List<FocusNode> getChildren() {
        return children;
    }

    public void setChildren(List<FocusNode> children) {
        this.children = children;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
