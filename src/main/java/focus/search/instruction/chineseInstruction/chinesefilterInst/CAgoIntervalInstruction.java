package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.filterInst.dateComplexInst.AgoIntervalInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/22
 * description:
 */
//<ago-filter> := <ago-days-filter> |
//        <ago-weeks-filter> |
//        <ago-months-filter> |
//        <ago-quarters-filter> |
//        <ago-years-filter> |
//        <ago-minutes-filter> |
//        <ago-hours-filter>;
public class CAgoIntervalInstruction {
    private static final Logger logger = Logger.getLogger(CAgoIntervalInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("CAgoIntervalInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        return AgoIntervalInstruction.build(focusPhrase, index, amb, formulas, dateColumns);
    }

}
