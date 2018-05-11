package focus.search.instruction.filterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<filter> := <simple-filter> |
//        <column-value>;
public class FilterInstruction {
    private static final Logger logger = Logger.getLogger(FilterInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        logger.info("Filter instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<simple-filter>":
                return SimpleFilterInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<column-value>":
                return ColumnValueInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
