package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Formula;

import java.util.ArrayList;
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

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws InvalidRuleException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(0);
        FocusNode param2 = focusPhrase.getFocusNodes().get(2);
        FocusNode symbol = focusPhrase.getFocusNodes().get(1).getChildren().getFirstNode();

        List<AnnotationToken> tokens = new ArrayList<>();
        tokens.addAll(NumberOrNumColInst.tokens(param1, formulas, amb));

        AnnotationToken token2 = new AnnotationToken();
        token2.value = symbol.getValue();
        token2.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token2.begin = symbol.getBegin();
        token2.end = symbol.getEnd();
        tokens.add(token2);

        tokens.addAll(NumberOrNumColInst.tokens(param2, formulas, amb));

        return tokens;
    }

}
