package focus.search.instruction.filterInst.dateComplexInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
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
//<last-days-filter> := last year |
//        <all-date-column> last year |
//        last <integer> years |
//        <all-date-column> last <integer> years;
public class LastYearsInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        return LastInstruction.build(focusPhrase, index, amb, formulas, dateColumns, "year");
    }

}
