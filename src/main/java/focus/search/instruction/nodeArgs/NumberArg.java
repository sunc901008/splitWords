package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.instruction.annotations.AnnotationToken;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
public class NumberArg {

    public static JSONObject arg(FocusNode focusNode) {
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

    public static AnnotationToken token(FocusNode focusNode) {
        AnnotationToken token = new AnnotationToken();
        FocusNode numberNode = focusNode.getChildren().getNodeNew(0);
        Object number;
        if (Constant.FNDType.INTEGER.equals(numberNode.getType())) {
            number = Integer.parseInt(numberNode.getValue());
        } else {
            number = Float.parseFloat(numberNode.getValue());
        }
        token.value = number;
        token.type = Constant.AnnotationTokenType.NUMBER;
        token.begin = numberNode.getBegin();
        token.end = numberNode.getEnd();
        return token;
    }

}
