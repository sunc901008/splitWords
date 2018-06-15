package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
public class ColumnValueInstruction {

    public static JSONArray build(FocusPhrase prePhrase, FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) {
        FocusNode fn = focusPhrase.getNodeNew(1);
        String columnValue = fn.getValue();
        Column column = prePhrase.getLastNode().getColumn();
        if (column != null) {

        }
        return null;
    }

    // 多个列中值
    public static JSONArray args(FocusNode focusNode) {
        JSONArray args = new JSONArray();
        FocusPhrase fp = focusNode.getChildren();
        for (int i = 1; i < fp.size(); i = i + 4) {
            JSONObject arg = new JSONObject();
            arg.put("type", Constant.InstType.STRING);
            arg.put("value", fp.getNodeNew(i).getValue());
            args.add(arg);
        }
        return args;
    }

    // 单个列中值
    public static JSONObject arg(FocusNode focusNode) {
        JSONObject arg = new JSONObject();
        FocusPhrase fp = focusNode.getChildren();
        arg.put("type", Constant.InstType.STRING);
        arg.put("value", fp.getNodeNew(1).getValue());
        return arg;
    }

    public static List<AnnotationToken> tokens(FocusNode focusNode) {
        FocusPhrase fp = focusNode.getChildren();
        List<AnnotationToken> tokens = new ArrayList<>();
        AnnotationToken first = new AnnotationToken();
        first.value = fp.getNodeNew(1).getValue();
        first.type = Constant.AnnotationTokenType.STRING;
        first.begin = fp.getNodeNew(0).getBegin();
        first.end = fp.getNodeNew(2).getEnd();
        tokens.add(first);
        for (int i = 5; i < fp.size(); i = i + 4) {
            FocusNode markNode = fp.getNodeNew(i - 2);
            AnnotationToken mark = new AnnotationToken();
            mark.value = markNode.getValue();
            mark.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
            mark.begin = markNode.getBegin();
            mark.end = markNode.getEnd();
            tokens.add(mark);
            AnnotationToken token = new AnnotationToken();
            token.value = fp.getNodeNew(i).getValue();
            token.type = Constant.AnnotationTokenType.STRING;
            token.begin = fp.getNodeNew(i - 1).getBegin();
            token.end = fp.getNodeNew(i + 1).getEnd();
            tokens.add(token);
        }
        return tokens;
    }

}
