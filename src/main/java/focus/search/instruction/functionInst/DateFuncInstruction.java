package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.DateFunc.AddDaysFuncInstruction;
import focus.search.instruction.functionInst.DateFunc.IfNullDateColFuncInstruction;
import focus.search.instruction.functionInst.DateFunc.IfThenElseDateColFuncInstruction;
import focus.search.instruction.functionInst.DateFunc.ToDateFuncInstruction;
import focus.search.instruction.functionInst.numberFunc.DaysFuncInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */

//<date-function-column> := <to_date-function> |
//        <date-function> |
//        <add-days-function> |
//          <start-of-month-function> |
//          <start-of-quarter-function> |
//          <start-of-week-function> |
//          <start-of-year-function>;
public class DateFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_date-function>":
                return ToDateFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<if-then-else-date-function>":
                return IfThenElseDateColFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<ifnull-date-function>":
                return IfNullDateColFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<date-function>":
            case "<start-of-month-function>":
            case "<start-of-quarter-function>":
            case "<start-of-week-function>":
            case "<start-of-year-function>":
                return DaysFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<add-days-function>":
                return AddDaysFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_date-function>":
                return ToDateFuncInstruction.arg(fn.getChildren(), formulas);
            case "<date-function>":
            case "<start-of-month-function>":
            case "<start-of-quarter-function>":
            case "<start-of-week-function>":
            case "<start-of-year-function>":
                return DaysFuncInstruction.arg(fn.getChildren(), formulas);
            case "<add-days-function>":
                return AddDaysFuncInstruction.arg(fn.getChildren(), formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_date-function>":
                return ToDateFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<date-function>":
            case "<start-of-month-function>":
            case "<start-of-quarter-function>":
            case "<start-of-week-function>":
            case "<start-of-year-function>":
                return DaysFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<add-days-function>":
                return AddDaysFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }
}
