package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.meta.Column;
import org.apache.log4j.Logger;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class ColumnInstruction {
    private static final Logger logger = Logger.getLogger(ColumnInstruction.class);

    public static JSONObject build(FocusPhrase focusPhrase) {
        logger.info("ColumnInstruction build. focusPhrase:" + focusPhrase.toJSON());
        JSONObject json = new JSONObject();
        json.put("hasTable", false);
        if (focusPhrase.size() > 1) {
            logger.info("size here");
            json.put("hasTable", true);
        }
        FocusNode fn = focusPhrase.getLastNode();
        logger.info(fn.toJSON());
        json.put("column", fn.getColumn());
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
