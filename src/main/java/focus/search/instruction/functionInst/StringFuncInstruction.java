package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.functionInst.StringFunc.*;
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
//        <if-then-else-string-function> |
//        <ifnull-string-function>;
public class StringFuncInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_string-function>":
                return ToStringFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<month-function>":
                return MonthFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<concat-function>":
                return ConcatFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<substr-function>":
                return SubstrFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<if-then-else-string-function>":
                return IfThenElseStringColFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<ifnull-string-function>":
                return IfNullStringColFuncInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_string-function>":
                return ToStringFuncInstruction.arg(fn.getChildren(), formulas);
            case "<month-function>":
                return MonthFuncInstruction.arg(fn.getChildren(), formulas);
            case "<concat-function>":
                return ConcatFuncInstruction.arg(fn.getChildren(), formulas);
            case "<substr-function>":
                return SubstrFuncInstruction.arg(fn.getChildren(), formulas);
            case "<if-then-else-string-function>":
                return IfThenElseStringColFuncInstruction.arg(fn.getChildren(), formulas);
            case "<ifnull-string-function>":
                return IfNullStringColFuncInstruction.arg(fn.getChildren(), formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
