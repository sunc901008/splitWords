package focus.search.bnf.tokens;

import focus.search.bnf.FocusNode;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusPhraseOrFilter {
    private List<FocusNode> focusNodes = new ArrayList<>();

    public List<FocusNode> getFocusNodes() {
        return focusNodes;
    }

    public void setFocusNodes(List<FocusNode> focusNodes) {
        this.focusNodes = focusNodes;
    }

    public void addPns(FocusNode fn) {
        this.focusNodes.add(fn);
    }

}
