package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.functionInst.BoolFuncColInstruction;
import focus.search.instruction.sourceInst.BoolColInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class BoolColOrBoolFuncColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws InvalidRuleException {
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
            case "<bool-function-column>":
                return BoolFuncColInstruction.build(focusNode.getChildren(), formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
