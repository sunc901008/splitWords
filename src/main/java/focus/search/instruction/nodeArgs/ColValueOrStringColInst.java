package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.instruction.sourceInst.StringColInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class ColValueOrStringColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        if (focusNode.getValue().equals("<string-columns>")) {
            return StringColInstruction.arg(focusNode.getChildren(), formulas);
        }
        // 列中值
        return ColumnValueInstruction.arg(focusNode);
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusNode focusNode, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        if (focusNode.getValue().equals("<string-columns>")) {
            return StringColInstruction.tokens(focusNode.getChildren(), formulas, amb);
        }
        // 列中值
        List<AnnotationToken> tokens = new ArrayList<>();
        tokens.addAll(ColumnValueInstruction.tokens(focusNode));
        return tokens;
    }

}
