package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
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
 * date: 2018/4/17
 * description:
 */

//<string-function-column> := <to_string-function> |
//        <month-function> |
//        <concat-function> |
//        <substr-function> |
//        <if-then-else-column-value-function> |
//        <if-then-else-string-column-function> |
//        <ifnull-column-value-function> |
//        <ifnull-string-column-function>;
public class StringFuncInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_string-function>":
                return ToBoolFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<month-function>":
                return ContainsFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<concat-function>":
                return StringFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<substr-function>":
                return StringFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<if-then-else-column-value-function>":
                return StringFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<if-then-else-string-column-function>":
                return StringFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<ifnull-column-value-function>":
                return StringFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<ifnull-string-column-function>":
                return StringFuncInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    public static JSONArray build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_string-function>":
            case "<month-function>":
            case "<concat-function>":
            case "<substr-function>":
            case "<if-then-else-column-value-function>":
            case "<if-then-else-string-column-function>":
            case "<ifnull-column-value-function>":
            case "<ifnull-string-column-function>":
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
