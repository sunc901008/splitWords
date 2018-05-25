package focus.search.instruction.chineseInstruction.chinesefilterInst;

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
//<after-chinese> := 之后 |
//        之后的 |
//        以后 |
//        以后的;
//
//<after-filter> := 在 <date-columns> <after-chinese> |
//        <date-columns> <after-chinese> |
//        在 <column-value> <after-chinese> |
//        <column-value> <after-chinese> |
//        <all-date-column> 在 <date-columns> <after-chinese> |
//        <all-date-column> 在 <column-value> <after-chinese>;
public class CAfterInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        return CBeforeAfterInstruction.build(focusPhrase, index, amb, formulas, dateColumns, "after");
    }
}
