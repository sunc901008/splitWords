package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.StringFunc.*;
import focus.search.instruction.functionInst.numberFunc.DaysFuncInstruction;
import focus.search.instruction.functionInst.otherFunc.IfNullFuncInstruction;
import focus.search.instruction.functionInst.otherFunc.IfThenElseFuncInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

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
//        <ifnull-string-function> |
//      <day-of-week-function> |
//    <time-function>
public class StringFuncInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
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
                return IfThenElseFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<ifnull-string-function>":
                return IfNullFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<day-of-week-function>":
            case "<time-function>":
                return DaysFuncInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
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
                return IfThenElseFuncInstruction.arg(fn.getChildren(), formulas);
            case "<ifnull-string-function>":
                return IfNullFuncInstruction.arg(fn.getChildren(), formulas);
            case "<day-of-week-function>":
            case "<time-function>":
                return DaysFuncInstruction.arg(fn.getChildren(), formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_string-function>":
                return ToStringFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<month-function>":
                return MonthFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<concat-function>":
                return ConcatFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<substr-function>":
                return SubstrFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<if-then-else-string-function>":
                return IfThenElseFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<ifnull-string-function>":
                return IfNullFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<day-of-week-function>":
            case "<time-function>":
                return DaysFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
