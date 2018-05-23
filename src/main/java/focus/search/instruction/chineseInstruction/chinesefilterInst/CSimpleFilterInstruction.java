package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.functionInst.BoolFuncColInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import org.apache.log4j.Logger;

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
//        <all-string-column> = <column-value> |
//        <bool-function-column>;
public class CSimpleFilterInstruction {
    private static final Logger logger = Logger.getLogger(CSimpleFilterInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        logger.info("SimpleFilter instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<number-columns>":
            case "<number>":
                return CFilterNumOrNumColInstruction.build(focusPhrase, index, amb, formulas);
            case "<bool-function-column>":
                return BoolFuncColInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<all-string-column>":
                return CFilterStringColEqualInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
