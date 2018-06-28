package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.numberFunc.*;
import focus.search.instruction.nodeArgs.BaseNumberFuncInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

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
//        <stddev-function> |
//        <variance-function> |
//        <unique-count-function> |
//        <cumulative-function> |
//        <moving-function> |
//        <group-function> |
//    <day-function> |
//    <day-number-of-week-function> |
//    <day-number-of-year-function> |
//    <diff-time-function> |
//    <hour-of-day-function> |
//    <greatest-function> |
//    <least-function> |
//    <abs-function> |
//    <acos-function> |
//    <asin-function> |
//    <atan-function> |
//    <cbrt-function> |
//    <ceil-function> |
//    <cos-function> |
//    <cube-function> |
//    <exp-function> |
//    <exp2-function> |
//    <floor-function> |
//    <ln-function> |
//    <log10-function> |
//    <log2-function> |
//    <sign-function> |
//      <sq-function> |
//      <sqrt-function> |
//      <tan-function> |
//    <atan2-function> |
//    <mod-function> |
//    <pow-function> |
//    <round-function> |
//    <safe-divide-function> |
//    <random-function> |
//    <strpos-function>;
public class NumberFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        return build1(fn.getChildren(), index, amb, formulas);
    }

    // 完整指令
    public static JSONArray build1(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        switch (focusPhrase.getInstName()) {
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
            case "<stddev-function>":
                return StddevFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<variance-function>":
                return VarianceFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<unique-count-function>":
                return UniqueCountFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<cumulative-function>":
                return CumulativeFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<moving-function>":
                return MovingFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<group-function>":
                return GroupFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<day-function>":
            case "<day-number-of-week-function>":
            case "<day-number-of-year-function>":
            case "<hour-of-day-function>":
                return DaysFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<diff-time-function>":
                return DiffTimeFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<abs-function>":
            case "<acos-function>":
            case "<asin-function>":
            case "<atan-function>":
            case "<cbrt-function>":
            case "<ceil-function>":
            case "<sin-function>":
            case "<cos-function>":
            case "<cube-function>":
            case "<exp-function>":
            case "<exp2-function>":
            case "<floor-function>":
            case "<ln-function>":
            case "<log10-function>":
            case "<log2-function>":
            case "<sign-function>":
            case "<sq-function>":
            case "<sqrt-function>":
            case "<tan-function>":
                return Math1FuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<greatest-function>":
            case "<least-function>":
            case "<atan2-function>":
            case "<mod-function>":
            case "<pow-function>":
            case "<round-function>":
            case "<safe-divide-function>":
                return Math2FuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<random-function>":
                return RandomFuncInstruction.build(focusPhrase, index);
            case "<strpos-function>":
                return StrposFuncInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        return arg(fn, formulas);
    }

    // 其他指令一部分
    public static JSONObject arg(FocusNode fn, List<Formula> formulas) throws FocusInstructionException, IllegalException {
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
            case "<stddev-function>":
                return StddevFuncInstruction.arg(fn.getChildren(), formulas);
            case "<variance-function>":
                return VarianceFuncInstruction.arg(fn.getChildren(), formulas);
            case "<unique-count-function>":
                return UniqueCountFuncInstruction.arg(fn.getChildren(), formulas);
            case "<cumulative-function>":
                return CumulativeFuncInstruction.arg(fn.getChildren(), formulas);
            case "<moving-function>":
                return MovingFuncInstruction.arg(fn.getChildren(), formulas);
            case "<group-function>":
                return GroupFuncInstruction.arg(fn.getChildren(), formulas);
            case "<day-function>":
            case "<day-number-of-week-function>":
            case "<day-number-of-year-function>":
            case "<hour-of-day-function>":
                return DaysFuncInstruction.arg(fn.getChildren(), formulas);
            case "<diff-time-function>":
                return DiffTimeFuncInstruction.arg(fn.getChildren(), formulas);
            case "<abs-function>":
            case "<acos-function>":
            case "<asin-function>":
            case "<atan-function>":
            case "<cbrt-function>":
            case "<ceil-function>":
            case "<sin-function>":
            case "<cos-function>":
            case "<cube-function>":
            case "<exp-function>":
            case "<exp2-function>":
            case "<floor-function>":
            case "<ln-function>":
            case "<log10-function>":
            case "<log2-function>":
            case "<sign-function>":
            case "<sq-function>":
            case "<sqrt-function>":
            case "<tan-function>":
                return Math1FuncInstruction.arg(fn.getChildren(), formulas);
            case "<greatest-function>":
            case "<least-function>":
            case "<atan2-function>":
            case "<mod-function>":
            case "<pow-function>":
            case "<round-function>":
            case "<safe-divide-function>":
                return Math2FuncInstruction.arg(fn.getChildren(), formulas);
            case "<random-function>":
                return RandomFuncInstruction.arg(fn.getChildren());
            case "<strpos-function>":
                return StrposFuncInstruction.arg(fn.getChildren(), formulas);
            default:
                throw new FocusInstructionException(fn.toJSON());
        }
    }

    // annotation
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
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
            case "<stddev-function>":
                return StddevFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<variance-function>":
                return VarianceFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<unique-count-function>":
                return UniqueCountFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<cumulative-function>":
                return CumulativeFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<moving-function>":
                return MovingFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<group-function>":
                return GroupFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<day-function>":
            case "<day-number-of-week-function>":
            case "<day-number-of-year-function>":
            case "<hour-of-day-function>":
                return DaysFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<diff-time-function>":
                return DiffTimeFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<abs-function>":
            case "<acos-function>":
            case "<asin-function>":
            case "<atan-function>":
            case "<cbrt-function>":
            case "<ceil-function>":
            case "<sin-function>":
            case "<cos-function>":
            case "<cube-function>":
            case "<exp-function>":
            case "<exp2-function>":
            case "<floor-function>":
            case "<ln-function>":
            case "<log10-function>":
            case "<log2-function>":
            case "<sign-function>":
            case "<sq-function>":
            case "<sqrt-function>":
            case "<tan-function>":
                return Math1FuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<greatest-function>":
            case "<least-function>":
            case "<atan2-function>":
            case "<mod-function>":
            case "<pow-function>":
            case "<round-function>":
            case "<safe-divide-function>":
                return Math2FuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<random-function>":
                return RandomFuncInstruction.tokens(fn.getChildren());
            case "<strpos-function>":
                return StrposFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
