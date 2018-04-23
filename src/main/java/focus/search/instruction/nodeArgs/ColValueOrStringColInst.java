package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.instruction.sourceInst.StringColInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class ColValueOrStringColInst {

    public static JSONObject arg(FocusNode focusNode, List<Formula> formulas) throws InvalidRuleException {
        if (focusNode.getValue().equals("<string-columns>")) {
            JSONObject json = StringColInstruction.build(focusNode.getChildren(), formulas);
            String type = json.getString("type");
            JSONObject arg = new JSONObject();
            // todo
            if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
                arg.put("type", "column");
                arg.put("value", ((Column) json.get("column")).getColumnId());
            } else if (Constant.InstType.FUNCTION.equals(type)) {
                arg = json.getJSONObject(Constant.InstType.FUNCTION);
            }
            return arg;
        }
        // 列中值
        return ColumnValueInstruction.arg(focusNode);
    }

}
