package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.exception.InvalidRuleException;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
public class NumberArg {

    public static JSONObject arg(FocusNode focusNode) throws InvalidRuleException {
        JSONObject arg = new JSONObject();
        FocusNode numberNode = focusNode.getChildren().getNodeNew(0);
        Object number;
        if (Constant.FNDType.INTEGER.equals(numberNode.getType())) {
            number = Integer.parseInt(numberNode.getValue());
        } else {
            number = Float.parseFloat(numberNode.getValue());
        }
        arg.put("type", Constant.InstType.NUMBER);
        arg.put("value", number);
        return arg;
    }

}
