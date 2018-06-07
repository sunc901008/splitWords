package focus.search.bnf;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.controller.common.FormulaAnalysis;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusPhrase {
    private static final Logger logger = Logger.getLogger(FocusPhrase.class);

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

    public void setFocusNodes(List<FocusNode> focusNodes) {
        this.focusNodes = focusNodes;
    }

    public FocusNode getFirstNode() {
        return allNode().get(0);
    }

    public FocusNode getLastNode() {
        List<FocusNode> all = allNode();
        return all.get(all.size() - 1);
    }

    public FocusNode getNodeNew(int index) {
        for (FocusNode fn : focusNodes) {
            if (fn.isHasChild()) {
                if (fn.getChildren().size() > index) {
                    return fn.getChildren().getNodeNew(index);
                }
                index = index - fn.getChildren().size();
            } else {
                if (index == 0) {
                    return fn;
                } else {
                    index--;
                }
            }
        }
        return null;
    }

    public List<FocusNode> allNode() {
        List<FocusNode> all = new ArrayList<>();
        for (FocusNode fn : focusNodes) {
            if (fn.isHasChild()) {
                all.addAll(fn.getChildren().allNode());
            } else {
                all.add(fn);
            }
        }
        return all;
    }

    public List<FocusNode> allFormulaNode() {
        List<FocusNode> all = new ArrayList<>();
        for (FocusNode fn : focusNodes) {
            if (fn.isHasChild() && !FormulaAnalysis.func.contains(fn.getValue())) {
                all.addAll(fn.getChildren().allFormulaNode());
            } else {
                all.add(fn);
            }
        }
        return all;
    }


    public FocusNode getNode(int index) {
        if (focusNodes.size() > index && index >= 0)
            return focusNodes.get(index);
        return null;
    }

    public void removeNodeNew(int index) {
        for (int i = 0; i < focusNodes.size(); i++) {
            FocusNode fn = focusNodes.get(i);
            if (fn.isHasChild()) {
                if (fn.getChildren().size() <= index) {
                    fn.getChildren().removeNodeNew(index);
                    return;
                }
                index = index - fn.getChildren().size();
            } else if (i == index) {
                focusNodes.remove(i);
                return;
            }
        }
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

    public void replaceNode(int index, FocusNode focusNode) {
        for (int i = 0; i < focusNodes.size(); i++) {
            FocusNode fn = focusNodes.get(i);
            if (fn.isHasChild()) {
                if (fn.getChildren().size() > index) {
                    fn.getChildren().replaceNode(index, focusNode);
                    return;
                }
                index = index - fn.getChildren().size();
            } else {
                if (index == 0) {
                    focusNodes.remove(i);
                    focusNodes.add(i, focusNode);
                    return;
                } else {
                    index--;
                }
            }
        }
    }

    public void addPns(List<FocusNode> focusNodes) {
        this.focusNodes.addAll(focusNodes);
    }

    public int size() {
        int count = 0;
        for (FocusNode fn : focusNodes) {
            if (fn.isHasChild()) {
                count = count + fn.getChildren().size();
            } else {
                count++;
            }
        }
        return count;
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

    public boolean equals(FocusPhrase fp) {
        if (this.size() != fp.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (!this.getNodeNew(i).getValue().equals(fp.getNodeNew(i).getValue())) {
                return false;
            }
        }
        return true;
    }

}
