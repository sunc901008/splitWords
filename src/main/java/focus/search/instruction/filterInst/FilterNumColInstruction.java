package focus.search.instruction.filterInst;

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
 * date: 2018/4/17
 * description:
 */

//<number-columns> <bool-symbol> <number>
//<number-columns> <bool-symbol> <number-columns>
public class FilterNumColInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode numberColumns = focusPhrase.getFocusNodes().get(0);
        NumberColInstruction.build(numberColumns, index, amb, formulas);
        FocusNode boolSymbol = focusNodes.get(1);
        String symbol = boolSymbol.getChildren().getNodeNew(0).getValue();
        FocusNode third = focusPhrase.getFocusNodes().get(2);
        if ("<number-columns>".equals(third.getValue())) {
            NumberColInstruction.build(third, index, amb, formulas);
        } else if ("<number>".equals(third.getValue())) {
            FocusNode numberNode = third.getChildren().getNodeNew(0);
            Object number;
            if (Constant.FNDType.INTEGER.equals(numberNode.getType())) {
                number = Integer.parseInt(numberNode.getValue());
            } else {
                number = Float.parseFloat(numberNode.getValue());
            }
        }
        return null;
    }

}
