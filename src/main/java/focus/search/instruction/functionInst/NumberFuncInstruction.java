package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.numberFunc.*;
import focus.search.instruction.nodeArgs.BaseNumberFuncInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<number-function-column> := <average-function> |
//        <count-function> |
//        <max-function> |
//        <min-function> |
//        <sum-function> |
//        <to_double-function> |
//        <to_integer-function> |
//        <diff_days-function> |
//        <month_number-function> |
//        <year-function> |
//        <strlen-function> |
//        <number-function> |
//        <if-then-else-number-function> |
//        <if-then-else-number-column-function> |
//        <ifnull-number-function> |
//        <ifnull-number-column-function>;
public class NumberFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<average-function>":
                return AverageFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<count-function>":
                return CountFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<max-function>":
            case "<min-function>":
                return MaxMinFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<sum-function>":
                return SumFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<to_double-function>":
            case "<to_integer-function>":
                return ToIntegerDoubleFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<diff_days-function>":
                return DiffDaysFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<month_number-function>":
            case "<year-function>":
                return MonthNumberYearFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<strlen-function>":
                return StrlenFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<number-function>":
                return BaseNumberFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<if-then-else-number-function>":
                return IfThenElseNumberColFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<ifnull-number-function>":
                return IfNullNumberColFuncInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        return arg(fn, formulas);
    }

    // 其他指令一部分
    public static JSONObject arg(FocusNode fn, List<Formula> formulas) throws InvalidRuleException {
        switch (fn.getValue()) {
            case "<average-function>":
                return AverageFuncInstruction.arg(fn.getChildren(), formulas);
            case "<count-function>":
                return CountFuncInstruction.arg(fn.getChildren(), formulas);
            case "<max-function>":
            case "<min-function>":
                return MaxMinFuncInstruction.arg(fn.getChildren(), formulas);
            case "<sum-function>":
                return SumFuncInstruction.arg(fn.getChildren(), formulas);
            case "<to_double-function>":
            case "<to_integer-function>":
                return ToIntegerDoubleFuncInstruction.arg(fn.getChildren(), formulas);
            case "<diff_days-function>":
                return DiffDaysFuncInstruction.arg(fn.getChildren(), formulas);
            case "<month_number-function>":
            case "<year-function>":
                return MonthNumberYearFuncInstruction.arg(fn.getChildren(), formulas);
            case "<strlen-function>":
                return StrlenFuncInstruction.arg(fn.getChildren(), formulas);
            case "<number-function>":
                return BaseNumberFuncInstruction.arg(fn.getChildren(), formulas);
            case "<if-then-else-number-function>":
                return IfThenElseNumberColFuncInstruction.arg(fn.getChildren(), formulas);
            case "<ifnull-number-function>":
                return IfNullNumberColFuncInstruction.arg(fn.getChildren(), formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    // annotation
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<average-function>":
                return AverageFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<count-function>":
                return CountFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<max-function>":
            case "<min-function>":
                return MaxMinFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<sum-function>":
                return SumFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<to_double-function>":
            case "<to_integer-function>":
                return ToIntegerDoubleFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<diff_days-function>":
                return DiffDaysFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<month_number-function>":
            case "<year-function>":
                return MonthNumberYearFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<strlen-function>":
                return StrlenFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<number-function>":
                return BaseNumberFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<if-then-else-number-function>":
                return IfThenElseNumberColFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<ifnull-number-function>":
                return IfNullNumberColFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
