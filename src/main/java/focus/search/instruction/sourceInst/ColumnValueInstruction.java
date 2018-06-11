package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
public class ColumnValueInstruction {

    // todo : curl index to search columnvalue for its column
    public static JSONArray build(FocusPhrase prePhrase, FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) {
        FocusNode fn = focusPhrase.getNodeNew(1);
        String columnValue = fn.getValue();
        Column column = prePhrase.getLastNode().getColumn();
        if (column != null) {

        }
        return null;
    }

    public static JSONObject arg(FocusNode focusNode) {
        JSONObject arg = new JSONObject();
        FocusPhrase fp = focusNode.getChildren();
        // todo 多个columnValue
        for (int i = 1; i < fp.size(); i = i + 4) {
            arg.put("type", Constant.InstType.STRING);
            arg.put("value", fp.getNodeNew(i).getValue());
        }
        return arg;
    }

    public static AnnotationToken token(FocusNode focusNode) {
        FocusPhrase fp = focusNode.getChildren();
        AnnotationToken token = new AnnotationToken();
        token.value = fp.getNodeNew(1).getValue();
        token.type = Constant.AnnotationTokenType.STRING;
        token.begin = fp.getFirstNode().getBegin();
        token.end = fp.getLastNode().getEnd();
        return token;
    }

}
