package focus.search.instruction.filterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.functionInst.BoolFuncColInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<simple-filter> := <number-columns> <bool-symbol> <number> |
//        <number> <bool-symbol> <number-columns> |
//        <number-columns> <bool-symbol> <number-columns> |
//        <number> <bool-symbol> <number> |
//        <bool-function-column>;
public class SimpleFilterInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<number-columns>":
            case "<number>":
                return FilterNumOrNumColInstruction.build(focusPhrase, index, amb, formulas);
            case "<bool-function-column>":
                return BoolFuncColInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
