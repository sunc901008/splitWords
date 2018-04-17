package focus.search.instruction.filterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.functionInst.BoolFuncInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
public class SimpleFilterInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<number-columns>":
                return FilterNumColInstruction.build(focusPhrase, index, amb, formulas);
            case "<number>":
                return NumberInstruction.build(focusPhrase, index, amb, formulas);
            case "<bool-function-column>":
                return BoolFuncInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
