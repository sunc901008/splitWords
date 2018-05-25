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
//<before-chinese> := 之前 |
//        之前的 |
//        以前 |
//        以前的;
//
//<before-filter> := 在 <date-columns> <before-chinese> |
//        <date-columns> <before-chinese> |
//        在 <column-value> <before-chinese> |
//        <column-value> <before-chinese> |
//        <all-date-column> 在 <date-columns> <before-chinese> |
//        <all-date-column> 在 <column-value> <before-chinese>;
public class CBeforeInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        return CBeforeAfterInstruction.build(focusPhrase, index, amb, formulas, dateColumns, "before");
    }

}
