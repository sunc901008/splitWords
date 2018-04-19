package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.functionInst.DateFunc.ToDateFuncInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */

//<date-function-column> := <to_date-function> |
//        <if-then-else-date-column-function> |
//        <ifnull-date-column-function>;
public class DateFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_date-function>":
                return ToDateFuncInstruction.build(focusPhrase, index, amb, formulas);
            case "<if-then-else-date-column-function>":
            case "<ifnull-date-column-function>":
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    // 其他指令一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_date-function>":
                return ToDateFuncInstruction.build(focusPhrase, formulas);
            case "<if-then-else-date-column-function>":
            case "<ifnull-date-column-function>":
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
