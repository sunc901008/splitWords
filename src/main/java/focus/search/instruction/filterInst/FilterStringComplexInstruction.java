package focus.search.instruction.filterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.filterInst.stringComplexInst.*;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/28
 * description:
 */
//<string-complex-filter> := <begins-with-filter> |
//        <not-begins-with-filter> |
//        <ends-with-filter> |
//        <not-ends-with-filter> |
//        <contains-filter> |
//        <not-contains-filter>;
public class FilterStringComplexInstruction {
    private static final Logger logger = Logger.getLogger(FilterStringComplexInstruction.class);

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("FilterStringComplexInstruction instruction arg. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<begins-with-filter>":
                return BeginsWithInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<not-begins-with-filter>":
                return NotBeginsWithInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<contains-filter>":
                return ContainsInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<not-contains-filter>":
                return NotContainsInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<ends-with-filter>":
                return EndsWithInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<not-ends-with-filter>":
                return NotEndsWithInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
