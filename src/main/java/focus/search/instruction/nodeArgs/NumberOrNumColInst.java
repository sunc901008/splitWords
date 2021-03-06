package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.instruction.sourceInst.NumberColInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class NumberOrNumColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        if ("<number-columns>".equals(focusNode.getValue())) {
            return NumberColInstruction.arg(focusNode.getChildren(), formulas);
        } else if ("<number>".equals(focusNode.getValue())) {
            return NumberArg.arg(focusNode);
        } else if ("<number-source-column>".equals(focusNode.getValue())) {
            return ColumnInstruction.arg(focusNode.getChildren(), formulas);
        }
        throw new FocusInstructionException(focusNode.toJSON());
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusNode focusNode, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<AnnotationToken> tokens = new ArrayList<>();
        if ("<number-columns>".equals(focusNode.getValue())) {
            return NumberColInstruction.tokens(focusNode.getChildren(), formulas, amb);
        } else if ("<number>".equals(focusNode.getValue())) {
            tokens.add(NumberArg.token(focusNode));
            return tokens;
        } else if ("<number-source-column>".equals(focusNode.getValue())) {
            tokens.add(AnnotationToken.singleCol(focusNode.getChildren(), amb, formulas));
            return tokens;
        }
        throw new FocusInstructionException(focusNode.toJSON());
    }
}
