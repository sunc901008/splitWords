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
public class BoolFuncColInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<contains-function>":
                return ContainsFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<and-function>":
                return AndOrFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<or-function>":
                return AndOrFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<if-then-else-bool-column-function>":
                return IfThenElseBoolColFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<ifnull-bool-column-function>":
                return IfNullBoolColFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<isnull-function>":
                return IsNullFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<not-function>":
                return NotFuncInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    // 其他指令一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.build(fn.getChildren(), formulas);
            case "<contains-function>":
                return ContainsFuncInstruction.build(focusPhrase, formulas);
            case "<and-function>":
                return AndOrFuncInstruction.build(focusPhrase, formulas);
            case "<or-function>":
                return AndOrFuncInstruction.build(focusPhrase, formulas);
            case "<if-then-else-bool-column-function>":
                return IfThenElseBoolColFuncInstruction.build(focusPhrase, formulas);
            case "<ifnull-bool-column-function>":
                return IfNullBoolColFuncInstruction.build(focusPhrase, formulas);
            case "<isnull-function>":
                return IsNullFuncInstruction.build(focusPhrase, formulas);
            case "<not-function>":
                return NotFuncInstruction.build(focusPhrase, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
