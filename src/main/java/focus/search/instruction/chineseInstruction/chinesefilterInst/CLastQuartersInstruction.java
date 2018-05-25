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
//<last-quarters-chinese> := 季度 |
//        季度的 |
//        季 |
//        季的 |
//        个季 |
//        个季的 |
//        个季度的 |
//        个季度的;
//<last-quarters-filter> := <last-chinese> <integer> <last-quarters-chinese> |
//        <all-date-column> <last-chinese> <integer> <last-quarters-chinese>;
public class CLastQuartersInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        return CLastInstruction.build(focusPhrase, index, amb, formulas, dateColumns, "quarter");
    }

}
