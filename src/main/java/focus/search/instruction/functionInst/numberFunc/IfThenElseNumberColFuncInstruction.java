package focus.search.instruction.functionInst.numberFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.BoolColOrBoolFuncColInst;
import focus.search.instruction.nodeArgs.NumberOrNumColInst;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<if-then-else-number-function> := if <bool-columns> then <number> else <number> |
//        if <bool-function-column> then <number> else <number> |
//        if <bool-columns> then <number-columns> else <number-columns> |
//        if <bool-function-column> then <number-columns> else <number-columns>;
public class IfThenElseNumberColFuncInstruction {

    // 完整指令 if-expression
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_EXPRESSION);

        json1.put("expression", arg(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);

        // annotation content
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);
        datas.addTokens(tokens(focusPhrase, formulas, amb));
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(1);
        FocusNode param2 = focusPhrase.getFocusNodes().get(3);
        FocusNode param3 = focusPhrase.getFocusNodes().get(5);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", "if-expression");
        JSONArray args = new JSONArray();
        args.add(BoolColOrBoolFuncColInst.arg(param1, formulas));
        args.add(NumberOrNumColInst.arg(param2, formulas));
        args.add(NumberOrNumColInst.arg(param3, formulas));
        expression.put("args", args);

        return expression;
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(1);
        FocusNode param2 = focusPhrase.getFocusNodes().get(3);
        FocusNode param3 = focusPhrase.getFocusNodes().get(5);

        List<AnnotationToken> tokens = new ArrayList<>();
        AnnotationToken token1 = new AnnotationToken();
        token1.value = focusPhrase.getFocusNodes().get(0).getValue();
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = focusPhrase.getFocusNodes().get(0).getBegin();
        token1.end = focusPhrase.getFocusNodes().get(0).getEnd();
        tokens.add(token1);

        tokens.addAll(BoolColOrBoolFuncColInst.tokens(param1, formulas, amb));

        AnnotationToken token2 = new AnnotationToken();
        token2.value = focusPhrase.getFocusNodes().get(2).getValue();
        token2.type = Constant.AnnotationTokenType.SYMBOL;
        token2.begin = focusPhrase.getFocusNodes().get(2).getBegin();
        token2.end = focusPhrase.getFocusNodes().get(2).getEnd();
        tokens.add(token2);

        tokens.addAll(NumberOrNumColInst.tokens(param2, formulas, amb));

        AnnotationToken token4 = new AnnotationToken();
        token4.value = focusPhrase.getFocusNodes().get(4).getValue();
        token4.type = Constant.AnnotationTokenType.SYMBOL;
        token4.begin = focusPhrase.getFocusNodes().get(4).getBegin();
        token4.end = focusPhrase.getFocusNodes().get(4).getEnd();
        tokens.add(token4);

        tokens.addAll(NumberOrNumColInst.tokens(param3, formulas, amb));

        return tokens;
    }

}
