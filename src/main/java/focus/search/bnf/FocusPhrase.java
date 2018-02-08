package focus.search.bnf;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusPhrase {
    private String instName;
    private String type = Constant.SUGGESTION;
    private List<FocusNode> focusNodes = new ArrayList<>();

    public FocusPhrase() {
    }

    public FocusPhrase(String instName) {
        this.instName = instName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstName() {
        return instName;
    }

    public void setInstName(String instName) {
        this.instName = instName;
    }

    public FocusNode getFirstNode() {
        return getNode(0);
    }

    public FocusNode getLastNode() {
        return getNode(size() - 1);
    }

    public FocusNode getNode(int index) {
        if (focusNodes.size() > index && index >= 0)
            return focusNodes.get(index);
        return null;
    }

    public void removeNode(int index) {
        if (focusNodes.size() > index) {
            this.focusNodes.remove(index);
        }
    }

    public boolean isSuggestion() {
        return Constant.SUGGESTION.equals(type);
    }

    public List<FocusNode> subNodes(int begin) {
        return subNodes(begin, size());
    }

    public List<FocusNode> subNodes(int begin, int end) {
        return focusNodes.subList(begin, end);
    }

    public List<FocusNode> getFocusNodes() {
        return this.focusNodes;
    }

    public void addPn(FocusNode fn) {
        this.focusNodes.add(fn);
    }

    public void addPn(int index, FocusNode fn) {
        this.focusNodes.add(index, fn);
    }

    public void addPns(List<FocusNode> focusNodes) {
        this.focusNodes.addAll(focusNodes);
    }

    public int size() {
        return this.focusNodes.size();
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("instName", instName);
        JSONArray jsonArray = new JSONArray();
        focusNodes.forEach(f -> jsonArray.add(f.toJSON()));
        json.put("focusNodes", jsonArray);
        json.put("type", type);
        return json;
    }

}
