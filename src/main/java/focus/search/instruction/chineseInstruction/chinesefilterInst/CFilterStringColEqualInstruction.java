package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.filterInst.FilterStringColEqualInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/9
 * description:
 */

//<all-string-column> = <column-value>
public class CFilterStringColEqualInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        return FilterStringColEqualInstruction.build(focusPhrase, index, amb, formulas);
    }

}
