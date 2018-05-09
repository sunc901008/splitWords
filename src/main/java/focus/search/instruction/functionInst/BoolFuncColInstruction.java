package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.boolFunc.*;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

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
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<contains-function>":
                return ContainsFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<and-function>":
            case "<or-function>":
                return AndOrFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<if-then-else-bool-column-function>":
                return IfThenElseBoolColFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<ifnull-bool-column-function>":
                return IfNullBoolColFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<isnull-function>":
                return IsNullFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<not-function>":
                return NotFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.arg(fn.getChildren(), formulas);
            case "<contains-function>":
                return ContainsFuncInstruction.arg(focusPhrase, formulas);
            case "<and-function>":
            case "<or-function>":
                return AndOrFuncInstruction.arg(focusPhrase, formulas);
            case "<if-then-else-bool-column-function>":
                return IfThenElseBoolColFuncInstruction.arg(focusPhrase, formulas);
            case "<ifnull-bool-column-function>":
                return IfNullBoolColFuncInstruction.arg(focusPhrase, formulas);
            case "<isnull-function>":
                return IsNullFuncInstruction.arg(focusPhrase, formulas);
            case "<not-function>":
                return NotFuncInstruction.arg(focusPhrase, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<contains-function>":
                return ContainsFuncInstruction.tokens(focusPhrase, formulas, amb);
            case "<and-function>":
            case "<or-function>":
                return AndOrFuncInstruction.tokens(focusPhrase, formulas, amb);
            case "<if-then-else-bool-column-function>":
                return IfThenElseBoolColFuncInstruction.tokens(focusPhrase, formulas, amb);
            case "<ifnull-bool-column-function>":
                return IfNullBoolColFuncInstruction.tokens(focusPhrase, formulas, amb);
            case "<isnull-function>":
                return IsNullFuncInstruction.tokens(focusPhrase, formulas, amb);
            case "<not-function>":
                return NotFuncInstruction.tokens(focusPhrase, formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
