package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.meta.Column;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class ColumnInstruction {

    public static JSONObject build(FocusPhrase focusPhrase) {
        JSONObject json = new JSONObject();
        json.put("hasTable", false);
        if (focusPhrase.size() > 1) {
            json.put("hasTable", true);
        }
        json.put("column", focusPhrase.getLastNode().getColumn());
        return json;
    }

    public static JSONObject arg(FocusPhrase focusPhrase) {
        JSONObject json = build(focusPhrase);
        JSONObject expression = new JSONObject();
        expression.put("type", "column");
        expression.put("value", ((Column) json.get("column")).getColumnId());
        return expression;
    }

}
