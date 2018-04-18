package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.sourceInst.NumberColInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class NumberOrNumColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws InvalidRuleException {
        JSONObject arg = new JSONObject();
        if ("<number-columns>".equals(focusNode.getValue())) {
            JSONObject jsonT = NumberColInstruction.build(focusNode.getChildren(), formulas);
            String typeT = jsonT.getString("type");
            // todo
            if (Constant.InstType.TABLE_COLUMN.equals(typeT) || Constant.InstType.COLUMN.equals(typeT)) {
                arg.put("type", "column");
                arg.put("value", ((Column) jsonT.get("column")).getColumnId());
            }
        } else if ("<number>".equals(focusNode.getValue())) {
            FocusNode numberNode = focusNode.getChildren().getNodeNew(0);
            Object number;
            if (Constant.FNDType.INTEGER.equals(numberNode.getType())) {
                number = Integer.parseInt(numberNode.getValue());
            } else {
                number = Float.parseFloat(numberNode.getValue());
            }
            arg.put("type", "number");
            arg.put("value", number);
        }
        return arg;
    }

}
