package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
//<bool-function> := <number-columns> <bool-symbol> <number> |
//        <number> <bool-symbol> <number-columns> |
//        <number-columns> <bool-symbol> <number-columns> |
//        <number> <bool-symbol> <number>;
public class BaseBoolFuncInstruction {

    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(0);
        FocusNode param2 = focusPhrase.getFocusNodes().get(2);
        FocusNode symbol = focusPhrase.getFocusNodes().get(1);

        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", symbol.getChildren().getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        args.add(NumberOrNumColInst.arg(param1, formulas));
        args.add(NumberOrNumColInst.arg(param2, formulas));

        expression.put("args", args);
        return expression;

    }

}
