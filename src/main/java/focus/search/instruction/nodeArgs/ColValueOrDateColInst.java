package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.instruction.sourceInst.DateColInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class ColValueOrDateColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws FocusInstructionException {
        if (focusNode.getValue().equals("<date-columns>")) {
            return DateColInstruction.arg(focusNode.getChildren(), formulas);
        }
        // 列中值
        JSONObject json = ColumnValueInstruction.arg(focusNode);
        json.put("type", Constant.InstType.DATE);
        return json;
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusNode focusNode, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        if (focusNode.getValue().equals("<date-columns>")) {
            return DateColInstruction.tokens(focusNode.getChildren(), formulas, amb);
        }
        // 列中值
        List<AnnotationToken> tokens = new ArrayList<>();
        AnnotationToken token = ColumnValueInstruction.token(focusNode);
        token.type = Constant.InstType.DATE;
        tokens.add(token);
        return tokens;
    }

}
