package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.filterInst.FilterDateInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<date-simple-filter> := <date-columns> <bool-symbol> <column-value> |
//        <column-value> <bool-symbol> <number-columns> |
//        <date-columns> <bool-symbol> <date-columns> |
//        <column-value> <bool-symbol> <column-value>;
public class CFilterDateInstruction {
    private static final Logger logger = Logger.getLogger(CFilterDateInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        logger.info("instruction arg. focusPhrase:" + focusPhrase.toJSON());
        return FilterDateInstruction.build(focusPhrase, index, amb, formulas);
    }

}
