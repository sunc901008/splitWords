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
        if (focusPhrase.size() > 1) {
            json.put("hasTable", true);
        }
        json.put("column", focusPhrase.getLastNode().getColumn());
        return json;
    }

    public static JSONObject arg(FocusPhrase focusPhrase) throws InvalidRuleException {
        JSONObject json = new JSONObject();
        json.put("hasTable", false);
        if (focusPhrase.size() > 1) {
            json.put("hasTable", true);
        }
        json.put("column", focusPhrase.getLastNode().getColumn());
        return json;
    }

}
