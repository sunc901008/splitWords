package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.BoolFuncColInstruction;
import focus.search.instruction.sourceInst.BoolColInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class BoolColOrBoolFuncColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        switch (focusNode.getValue()) {
            case "<bool-columns>":
                return BoolColInstruction.arg(focusNode.getChildren(), formulas);
            case "<no-or-and-bool-function-column>":
                return NoOrAndBoolFuncColInstruction.arg(focusNode.getChildren(), formulas);
            case "<bool-function-column>":
                return BoolFuncColInstruction.arg(focusNode.getChildren(), formulas);
            case "<if-then-else-bool-filter>":
            case "<bool-function>":
                return BaseBoolFuncInstruction.arg(focusNode.getChildren(), formulas);
            default:
                throw new FocusInstructionException(focusNode.toJSON());
        }
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusNode focusNode, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        switch (focusNode.getValue()) {
            case "<bool-columns>":
                return BoolColInstruction.tokens(focusNode, formulas, amb);
            case "<no-or-and-bool-function-column>":
                return NoOrAndBoolFuncColInstruction.tokens(focusNode.getChildren(), formulas, amb);
            case "<bool-function-column>":
                return BoolFuncColInstruction.tokens(focusNode.getChildren(), formulas, amb);
            case "<bool-function>":
                return BaseBoolFuncInstruction.tokens(focusNode.getChildren(), formulas, amb);
            default:
                throw new FocusInstructionException(focusNode.toJSON());
        }
    }

}
