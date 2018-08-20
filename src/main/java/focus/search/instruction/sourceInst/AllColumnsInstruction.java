package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
//<all-columns> := <number-columns> |
//        <string-columns> |
//        <bool-columns> |
//        <date-columns>;
public class AllColumnsInstruction {

    // 完整指令 all-columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<bool-columns>":
                return BoolColInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<date-columns>":
                return DateColInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<number-columns>":
                return NumberColInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<string-columns>":
                return StringColInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<bool-columns>":
                return BoolColInstruction.arg(fn.getChildren(), formulas);
            case "<date-columns>":
                return DateColInstruction.arg(fn.getChildren(), formulas);
            case "<number-columns>":
                return NumberColInstruction.arg(fn.getChildren(), formulas);
            case "<string-columns>":
                return StringColInstruction.arg(fn.getChildren(), formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<bool-columns>":
                return BoolColInstruction.tokens(fn, formulas, amb);
            case "<date-columns>":
                return DateColInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<number-columns>":
                return NumberColInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<string-columns>":
                return StringColInstruction.tokens(fn.getChildren(), formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
