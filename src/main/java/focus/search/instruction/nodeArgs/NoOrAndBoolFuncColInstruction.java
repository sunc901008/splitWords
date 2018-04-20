package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.functionInst.boolFunc.ContainsFuncInstruction;
import focus.search.instruction.functionInst.boolFunc.ToBoolFuncInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
//<no-or-and-bool-function-column> := <to_bool-function> |
//        <contains-function> |
//        <bool-function> |
//        <if-then-else-bool-column-function> |
//        <ifnull-bool-column-function> |
//        <isnull-function> |
//        <not-function>;
public class NoOrAndBoolFuncColInstruction {

    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        JSONObject res = new JSONObject();
        switch (node.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.arg(node.getChildren(), formulas);
            case "<contains-function>":
                return ContainsFuncInstruction.arg(node.getChildren(), formulas);
            case "<bool-function>":
                return BaseBoolFuncInstruction.arg(node.getChildren(), formulas);
            case "<if-then-else-bool-column-function>":
                return ToBoolFuncInstruction.arg(node.getChildren(), formulas);
            case "<ifnull-bool-column-function>":
                return ToBoolFuncInstruction.arg(node.getChildren(), formulas);
            case "<isnull-function>":
                return res;
            case "<not-function>":
                return res;
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
