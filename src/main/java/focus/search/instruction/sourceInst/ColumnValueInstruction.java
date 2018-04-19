package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
public class ColumnValueInstruction {

    public static JSONObject arg(FocusNode focusNode) throws InvalidRuleException {
        JSONObject arg = new JSONObject();
        FocusPhrase fp = focusNode.getChildren();
        // todo 多个columnValue
        for (int i = 2; i < fp.size(); i = i + 4) {
            arg.put("type", Constant.InstType.TABLE_COLUMN);
            arg.put("value", fp.getNodeNew(i).getValue());
        }
        return arg;
    }

}
