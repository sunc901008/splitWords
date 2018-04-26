package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.instruction.sourceInst.StringColInstruction;
import focus.search.meta.Formula;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class ColValueOrStringColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws InvalidRuleException {
        if (focusNode.getValue().equals("<string-columns>")) {
            return StringColInstruction.arg(focusNode.getChildren(), formulas);
        }
        // 列中值
        return ColumnValueInstruction.arg(focusNode);
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusNode focusNode, List<Formula> formulas, JSONObject amb) throws InvalidRuleException {
        if (focusNode.getValue().equals("<string-columns>")) {
            return StringColInstruction.tokens(focusNode.getChildren(), formulas, amb);
        }
        // 列中值
        List<AnnotationToken> tokens = new ArrayList<>();
        tokens.add(ColumnValueInstruction.token(focusNode));
        return tokens;
    }


}
