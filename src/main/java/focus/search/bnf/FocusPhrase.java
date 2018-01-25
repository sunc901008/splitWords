package focus.search.bnf;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusPhrase {
    private String instName;
    private List<FocusNode> focusNodes = new ArrayList<>();

    public FocusPhrase() {
    }

    public FocusPhrase(String instName) {
        this.instName = instName;
    }

    public String getInstName() {
        return instName;
    }

    public void setInstName(String instName) {
        this.instName = instName;
    }

    public FocusNode getFirstNode() {
        if (focusNodes.isEmpty())
            return null;
        return focusNodes.get(0);
    }

    public List<FocusNode> subNodes(int begin) {
        return subNodes(begin, size());
    }

    public List<FocusNode> subNodes(int begin, int end) {
        return focusNodes.subList(begin, end);
    }

    public void setFocusNodes(List<FocusNode> focusNodes) {
        this.focusNodes = focusNodes;
    }

    public void addPn(FocusNode fn) {
        this.focusNodes.add(fn);
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
        return json;
    }

}
