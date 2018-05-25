package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.filterInst.dateComplexInst.LastInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/25
 * description:
 */
//<last-days-chinese> := 天 |
//        天的;
//<last-days-filter> := <last-chinese> <integer> <last-days-chinese> |
//        <all-date-column> <last-chinese> <integer> <last-days-chinese>;
public class CLastDaysInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        return CLastInstruction.build(focusPhrase, index, amb, formulas, dateColumns, "day");
    }

}
