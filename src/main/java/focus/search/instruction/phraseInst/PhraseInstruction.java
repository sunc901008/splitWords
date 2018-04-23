package focus.search.instruction.phraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.sourceInst.AllColumnsInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<phrase> := <top-n> |
//        <bottom-n> |
//        <sort-by> |
//        <growth-of> |
//        <all-columns>;
public class PhraseInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<top-n>":
            case "<bottom-n>":
                return TopBottomInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<sort-by>":
                return SortByInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<growth-of>":
                return GrowthOfInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<all-columns>":
                return AllColumnsInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
