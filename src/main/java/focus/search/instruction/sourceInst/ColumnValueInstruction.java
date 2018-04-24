package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
public class ColumnValueInstruction {

    // todo : curl index to search columnvalue for its column
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);

        return null;
    }

    public static JSONObject arg(FocusNode focusNode) throws InvalidRuleException {
        JSONObject arg = new JSONObject();
        FocusPhrase fp = focusNode.getChildren();
        // todo 多个columnValue
        for (int i = 1; i < fp.size(); i = i + 4) {
            arg.put("type", Constant.InstType.STRING);
            arg.put("value", fp.getNodeNew(i).getValue());
        }
        return arg;
    }

}
