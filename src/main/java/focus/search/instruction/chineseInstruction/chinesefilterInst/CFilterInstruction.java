package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.functionInst.BoolFuncColInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<filter> := <number-simple-filter> |
//        <date-simple-filter> |
//        <date-complex-filter> |
//        <string-simple-filter> |
//        <string-complex-filter> |
//        <bool-function-column>;
public class CFilterInstruction {
    private static final Logger logger = Logger.getLogger(CFilterInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("Filter instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<number-simple-filter>":
                return CFilterNumOrNumColInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<date-simple-filter>":
                return CFilterDateInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<date-complex-filter>":
                return CFilterDateComplexInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<string-simple-filter>":
                return CFilterStringColEqualInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<string-complex-filter>":
                return CFilterStringComplexInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<bool-function-column>":
                return BoolFuncColInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
