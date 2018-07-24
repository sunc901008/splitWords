package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/7/24
 * description:
 */
//(<bool-function>)
public class BracketBaseBoolFuncInstruction {

    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        return BaseBoolFuncInstruction.arg(focusPhrase.getFocusNodes().get(1).getChildren(), formulas);

    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode leftBracket = focusNodes.get(0);
        FocusNode rightBracket = focusNodes.get(2);

        List<AnnotationToken> tokens = new ArrayList<>();

        AnnotationToken token1 = new AnnotationToken();
        token1.value = leftBracket.getValue();
        token1.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token1.begin = leftBracket.getBegin();
        token1.end = leftBracket.getEnd();
        tokens.add(token1);

        tokens.addAll(BaseBoolFuncInstruction.tokens(focusNodes.get(1).getChildren(), formulas, amb));

        AnnotationToken token3 = new AnnotationToken();
        token3.value = rightBracket.getValue();
        token3.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token3.begin = rightBracket.getBegin();
        token3.end = rightBracket.getEnd();
        tokens.add(token3);

        return tokens;
    }

}
