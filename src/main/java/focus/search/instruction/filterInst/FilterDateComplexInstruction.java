package focus.search.instruction.filterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.filterInst.dateComplexInst.*;
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
//<date-complex-filter> := <before-after-filter>|
//                         <last-filter> |
//                         <next-filter> |
//                         <between-and-filter> |
//                          <date-interval> |
//                          <ago-filter>;
public class FilterDateComplexInstruction {
    private static final Logger logger = Logger.getLogger(FilterDateComplexInstruction.class);

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("FilterDateComplexInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<before-after-filter>":
                return BeforeAfterInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<last-filter>":
                return LastInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<next-filter>":
                return NextInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<between-and-filter>":
                return BetweenAndInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<date-interval>":
                return DateIntervalInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<ago-filter>":
                return AgoIntervalInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
