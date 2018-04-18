package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
public class NumberFuncInstruction {

    // todo
    public static JSONArray build(FocusPhrase focusPhrase) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        return null;
    }

}
