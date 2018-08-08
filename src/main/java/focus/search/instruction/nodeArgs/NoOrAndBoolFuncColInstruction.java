package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.boolFunc.*;
import focus.search.instruction.functionInst.numberFunc.DaysFuncInstruction;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
//        <isnull-function> |
//        <not-function>|
//        <bool-columns>;
//<no-or-and-bool-function-column> := <to_bool-function> |
//        <contains-function> |
//        <bool-function> |
//        ( <bool-function> ) |
//        <isnull-function> |
//        <not-function> |
//        <is-weekend-function> |
//        <bool-columns>;

public class NoOrAndBoolFuncColInstruction {

    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        switch (node.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.arg(node.getChildren(), formulas);
            case "<contains-function>":
                return ContainsFuncInstruction.arg(node.getChildren(), formulas);
            case "(":
                return BracketBaseBoolFuncInstruction.arg(focusPhrase, formulas);
            case "<bool-function>":
                return BaseBoolFuncInstruction.arg(node.getChildren(), formulas);
            case "<if-then-else-bool-column-function>":
                return ToBoolFuncInstruction.arg(node.getChildren(), formulas);
            case "<ifnull-bool-column-function>":
                return ToBoolFuncInstruction.arg(node.getChildren(), formulas);
            case "<isnull-function>":
                return IsNullFuncInstruction.arg(node.getChildren(), formulas);
            case "<not-function>":
                return NotFuncInstruction.arg(node.getChildren(), formulas);
            case "<is-weekend-function>":
                return DaysFuncInstruction.arg(node.getChildren(), formulas);
            case "<bool-columns>":
                return ColumnInstruction.arg(node.getChildren(), formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        switch (node.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.tokens(node.getChildren(), formulas, amb);
            case "<contains-function>":
                return ContainsFuncInstruction.tokens(node.getChildren(), formulas, amb);
            case "(":
                return BracketBaseBoolFuncInstruction.tokens(focusPhrase, formulas, amb);
            case "<bool-function>":
                return BaseBoolFuncInstruction.tokens(node.getChildren(), formulas, amb);
            case "<if-then-else-bool-column-function>":
                return IfThenElseBoolColFuncInstruction.tokens(node.getChildren(), formulas, amb);
            case "<ifnull-bool-column-function>":
                return IfNullBoolColFuncInstruction.tokens(node.getChildren(), formulas, amb);
            case "<isnull-function>":
                return IsNullFuncInstruction.tokens(node.getChildren(), formulas, amb);
            case "<not-function>":
                return NotFuncInstruction.tokens(node.getChildren(), formulas, amb);
            case "<is-weekend-function>":
                return DaysFuncInstruction.tokens(node.getChildren(), formulas, amb);
            case "<bool-columns>":
                List<AnnotationToken> tokens = new ArrayList<>();
                FocusPhrase fp = node.getChildren();
                int begin = fp.getFirstNode().getBegin();
                int end = fp.getLastNode().getEnd();
                tokens.add(AnnotationToken.singleCol(fp.getLastNode().getColumn(), fp.size() == 2, begin, end, amb));
                return tokens;
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
