package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
//<all-columns> := <number-columns> |
//        <string-columns> |
//        <bool-columns> |
//        <date-columns>;
public class AllColumnsInstruction {

    // todo
    // 完整指令 all-columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<bool-columns>":
                return BoolColInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<date-columns>":
                return DateColInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<number-columns>":
                return NumberColInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<string-columns>":
                return StringColInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    // 其他指令的一部分
//    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
//        JSONObject json = build(focusPhrase, formulas);
//        String type = json.getString("type");
//        JSONObject arg = new JSONObject();
//        // todo
//        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
//            arg.put("type", "column");
//            arg.put("value", ((Column) json.get("column")).getColumnId());
//        } else if (Constant.InstType.FUNCTION.equals(type)) {
//            arg = json.getJSONObject(Constant.InstType.FUNCTION);
//        }
//        return arg;
//    }

    // 其他指令的一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<bool-columns>":
                return BoolColInstruction.build(fn.getChildren(), formulas);
            case "<date-columns>":
                return DateColInstruction.build(fn.getChildren(), formulas);
            case "<number-columns>":
                return NumberColInstruction.build(fn.getChildren(), formulas);
            case "<string-columns>":
                return StringColInstruction.build(fn.getChildren(), formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }
}
