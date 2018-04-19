package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Formula;

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

    // todo
    // 完整指令 all-columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<number-columns>":
                return NumberColInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<string-columns>":
                return StringColInstruction.build(focusPhrase, index, amb, formulas);
            case "<bool-columns>":
                return BoolColInstruction.build(focusPhrase, index, amb, formulas);
            case "<date-columns>":
                return DateColInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    // 其他指令的一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<number-columns>":
                return NumberColInstruction.build(fn.getChildren(), formulas);
            case "<string-columns>":
                return StringColInstruction.build(focusPhrase, formulas);
            case "<bool-columns>":
                return BoolColInstruction.build(focusPhrase, formulas);
            case "<date-columns>":
                return DateColInstruction.build(focusPhrase, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }
}
