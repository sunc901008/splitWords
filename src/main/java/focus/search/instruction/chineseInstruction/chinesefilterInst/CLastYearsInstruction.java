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
//<last-years-chinese> := 年 |
//        年的;
//<last-years-filter> := <last-chinese> <integer> <last-years-chinese> |
//        <all-date-column> <last-chinese> <integer> <last-years-chinese>;
public class CLastYearsInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        return CLastInstruction.build(focusPhrase, index, amb, formulas, dateColumns, "year");
    }

}
