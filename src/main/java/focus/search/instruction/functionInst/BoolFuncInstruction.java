package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.functionInst.boolFunc.*;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<bool-function-column> := <to_bool-function> |
//        <contains-function> |
//        <and-function> |
//        <or-function> |
//        <if-then-else-bool-column-function> |
//        <ifnull-bool-column-function> |
//        <isnull-function> |
//        <not-function>;
public class BoolFuncInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<contains-function>":
                return ContainsFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<and-function>":
                return BoolFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<or-function>":
                return BoolFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<if-then-else-bool-column-function>":
                return BoolFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<ifnull-bool-column-function>":
                return BoolFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<isnull-function>":
                return BoolFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<not-function>":
                return BoolFuncInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
