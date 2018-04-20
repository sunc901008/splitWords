package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.functionInst.DateFuncInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<date-columns> := <all-date-column> |
//        <date-function-column>;
public class DateColInstruction {

    // todo
    // 完整指令 columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode numberColumns = focusPhrase.getFocusNodes().get(0);
        return null;
    }

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        JSONObject json = build(focusPhrase, formulas);
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

    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        JSONObject res = new JSONObject();
        switch (node.getValue()) {
            case "<all-date-column>":
                JSONObject json = ColumnInstruction.build(node.getChildren());
                if (json.getBoolean("hasTable")) {
                    res.put("type", Constant.InstType.TABLE_COLUMN);
                } else {
                    res.put("type", Constant.InstType.COLUMN);
                }
                res.put("column", json.get("column"));
                return res;
            case "<date-function-column>":
                res.put("type", Constant.InstType.FUNCTION);
                res.put("function", DateFuncInstruction.arg(node.getChildren(), formulas));
                return res;
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }
}
