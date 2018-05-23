package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.BoolFuncColInstruction;
import focus.search.instruction.sourceInst.BoolColInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class BoolColOrBoolFuncColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws FocusInstructionException {
        switch (focusNode.getValue()) {
            case "<bool-columns>":
                JSONObject arg = new JSONObject();
                JSONObject json = BoolColInstruction.build(focusNode.getChildren(), formulas);
                String type = json.getString("type");
                if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
                    arg.put("type", "column");
                    arg.put("value", ((Column) json.get("column")).getColumnId());
                }
                return arg;
            case "<no-or-and-bool-function-column>":
                return NoOrAndBoolFuncColInstruction.arg(focusNode.getChildren(), formulas);
            case "<bool-function-column>":
                return BoolFuncColInstruction.arg(focusNode.getChildren(), formulas);
            default:
                throw new FocusInstructionException(focusNode.toJSON());
        }
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusNode focusNode, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        switch (focusNode.getValue()) {
            case "<bool-columns>":
                return BoolColInstruction.tokens(focusNode, formulas, amb);
            case "<no-or-and-bool-function-column>":
                return NoOrAndBoolFuncColInstruction.tokens(focusNode.getChildren(), formulas, amb);
            case "<bool-function-column>":
                return BoolFuncColInstruction.tokens(focusNode.getChildren(), formulas, amb);
            default:
                throw new FocusInstructionException(focusNode.toJSON());
        }
    }

}
