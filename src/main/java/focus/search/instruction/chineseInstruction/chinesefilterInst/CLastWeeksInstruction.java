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
//<last-weeks-chinese> := 周 |
//        周的 |
//        星期 |
//        星期的 |
//        个星期 |
//        个星期的;
//<last-weeks-filter> := <last-chinese> <integer> <last-weeks-chinese> |
//        <all-date-column> <last-chinese> <integer> <last-weeks-chinese>;
public class CLastWeeksInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        return CLastInstruction.build(focusPhrase, index, amb, formulas, dateColumns, "week");
    }

}
