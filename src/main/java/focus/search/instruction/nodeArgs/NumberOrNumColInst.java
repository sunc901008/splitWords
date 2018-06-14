package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.NumberColInstruction;
import focus.search.meta.Column;
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
public class NumberOrNumColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        JSONObject arg = new JSONObject();
        if ("<number-columns>".equals(focusNode.getValue())) {
            JSONObject json = NumberColInstruction.build(focusNode.getChildren(), formulas);
            String type = json.getString("type");
            if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
                arg.put("type", Constant.InstType.COLUMN);
                arg.put("value", ((Column) json.get("column")).getColumnId());
            } else if (Constant.InstType.FUNCTION.equals(type)) {
                arg = json.getJSONObject(Constant.InstType.FUNCTION);
            }
            return arg;
        } else if ("<number>".equals(focusNode.getValue())) {
            return NumberArg.arg(focusNode);
        }
        throw new FocusInstructionException(focusNode.toJSON());
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusNode focusNode, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        if ("<number-columns>".equals(focusNode.getValue())) {
            return NumberColInstruction.tokens(focusNode.getChildren(), formulas, amb);
        } else if ("<number>".equals(focusNode.getValue())) {
            List<AnnotationToken> tokens = new ArrayList<>();
            tokens.add(NumberArg.token(focusNode));
            return tokens;
        }
        throw new FocusInstructionException(focusNode.toJSON());
    }
}
