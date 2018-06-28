package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.ColumnInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/27
 * description:
 */
public class BaseFunc {

    //<attribute-list-params> := <not-number-source-column> |
    //                           <not-number-source-column> , <attribute-list-params>;
    public static JSONArray attributeListArgs(FocusPhrase focusPhrase) {
        JSONArray args = new JSONArray();
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        args.add(ColumnInstruction.arg(focusNodes.get(0).getChildren()));
        if (focusNodes.size() > 1) {
            args.addAll(attributeListArgs(focusNodes.get(2).getChildren()));
        }
        return args;
    }

    //<attribute-list-params> := <not-number-source-column> |
    //                           <not-number-source-column> , <attribute-list-params>;
    public static List<AnnotationToken> attributeListTokens(FocusPhrase focusPhrase, JSONObject amb) {
        List<AnnotationToken> tokens = new ArrayList<>();
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        tokens.add(AnnotationToken.singleCol(focusNodes.get(0).getChildren(), amb));
        if (focusNodes.size() > 1) {
            AnnotationToken token1 = new AnnotationToken();
            token1.value = focusNodes.get(1).getValue();
            token1.type = Constant.AnnotationTokenType.SYMBOL;
            token1.begin = focusNodes.get(1).getBegin();
            token1.end = focusNodes.get(1).getEnd();
            tokens.add(token1);
            tokens.addAll(attributeListTokens(focusNodes.get(2).getChildren(), amb));
        }
        return tokens;
    }

}
