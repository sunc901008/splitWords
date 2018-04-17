package focus.search.instruction.filterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
public class FilterInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<simple-filter>":
                return SimpleFilterInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<column-value>":
                return ColValueInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
