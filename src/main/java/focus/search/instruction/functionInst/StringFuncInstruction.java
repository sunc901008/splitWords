package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.StringFunc.*;
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
//        <ifnull-string-function>;
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
                return IfThenElseStringColFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<ifnull-string-function>":
                return IfNullStringColFuncInstruction.build(focusPhrase, index, amb, formulas);
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
                return IfThenElseStringColFuncInstruction.arg(fn.getChildren(), formulas);
            case "<ifnull-string-function>":
                return IfNullStringColFuncInstruction.arg(fn.getChildren(), formulas);
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
                return IfThenElseStringColFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<ifnull-string-function>":
                return IfNullStringColFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
