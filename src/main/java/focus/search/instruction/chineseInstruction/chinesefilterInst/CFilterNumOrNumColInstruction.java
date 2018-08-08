package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.filterInst.FilterNumOrNumColInstruction;
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

//<number-columns> <bool-symbol> <number>
//<number-columns> <bool-symbol> <number-columns>
//<number> <bool-symbol> <number-columns>
//<number> <bool-symbol> <number>

public class CFilterNumOrNumColInstruction {
    private static final Logger logger = Logger.getLogger(CFilterNumOrNumColInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        logger.info("instruction arg. focusPhrase:" + focusPhrase.toJSON());
        return FilterNumOrNumColInstruction.build(focusPhrase, index, amb, formulas);
    }

}
