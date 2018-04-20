package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class ColumnInstruction {

    // todo  formula
    public static JSONObject build(FocusPhrase focusPhrase) throws InvalidRuleException {
        JSONObject json = new JSONObject();
        json.put("hasTable", false);
        if (focusPhrase.getFocusNodes().get(0).getValue().contains("table")) {
            json.put("hasTable", true);
        }
        json.put("column", focusPhrase.getLastNode().getColumn());
        return json;
    }

}
