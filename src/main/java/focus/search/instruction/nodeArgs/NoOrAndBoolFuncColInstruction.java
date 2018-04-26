package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.boolFunc.*;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.meta.Formula;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
//<no-or-and-bool-function-column> := <to_bool-function> |
//        <contains-function> |
//        <bool-function> |
//        <if-then-else-bool-column-function> |
//        <ifnull-bool-column-function> |
//        <isnull-function> |
//        <not-function>|
//        <bool-columns>;

public class NoOrAndBoolFuncColInstruction {

    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        switch (node.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.arg(node.getChildren(), formulas);
            case "<contains-function>":
                return ContainsFuncInstruction.arg(node.getChildren(), formulas);
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
            case "<bool-columns>":
                return ColumnInstruction.arg(node.getChildren());
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws InvalidRuleException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        switch (node.getValue()) {
            case "<to_bool-function>":
                return ToBoolFuncInstruction.tokens(node.getChildren(), formulas, amb);
            case "<contains-function>":
                return ContainsFuncInstruction.tokens(node.getChildren(), formulas, amb);
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
            case "<bool-columns>":
                List<AnnotationToken> tokens = new ArrayList<>();
                FocusPhrase fp = node.getChildren();
                int begin = fp.getFirstNode().getBegin();
                int end = fp.getLastNode().getEnd();
                tokens.add(AnnotationToken.singleCol(fp.getLastNode().getColumn(), fp.size() == 2, begin, end, amb));
                return tokens;
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
