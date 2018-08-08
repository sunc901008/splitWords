package focus.search.instruction.functionInst.otherFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.Base;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.OtherFuncInstruction;
import focus.search.instruction.nodeArgs.BaseBoolFuncInstruction;
import focus.search.instruction.nodeArgs.BoolColOrBoolFuncColInst;
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
//<if-then-else-bool-filter> := <bool-columns> |
//        <bool-function>;
//<if-then-else-function> := if <if-then-else-bool-filter> then <all-string-column> else <single-column-value> |
//        if <if-then-else-bool-filter> then <all-string-column> else <all-string-column> |
//        if <if-then-else-bool-filter> then <single-column-value> else <single-column-value> |
//        if <if-then-else-bool-filter> then <single-column-value> else <all-string-column> |
//        if <if-then-else-bool-filter> then <number-source-column> else <number> |
//        if <if-then-else-bool-filter> then <number-source-column> else <number-source-column> |
//        if <if-then-else-bool-filter> then <number> else <number> |
//        if <if-then-else-bool-filter> then <number> else <number-source-column> |
//        if <if-then-else-bool-filter> then <all-bool-column> else <all-bool-column> |
//        if <if-then-else-bool-filter> then <all-date-column> else <date-string-value> |
//        if <if-then-else-bool-filter> then <all-date-column> else <all-date-column> |
//        if <if-then-else-bool-filter> then <date-string-value> else <date-string-value> |
//        if <if-then-else-bool-filter> then <date-string-value> else <all-date-column>;
public class IfThenElseFuncInstruction {

    // 完整指令 if-expression
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_EXPRESSION);

        json1.put("expression", arg(focusPhrase, formulas));
        json1.put("name", Base.InstName(focusPhrase));
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
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(1).getChildren().getFocusNodes().get(0);
        FocusNode param2 = focusNodes.get(3);
        FocusNode param3 = focusNodes.get(5);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", "if-expression");
        JSONArray args = new JSONArray();
        if ("<bool-columns>".equals(param1.getValue())) {
            args.add(BoolColOrBoolFuncColInst.arg(param1, formulas));
        } else {
            args.add(BaseBoolFuncInstruction.arg(param1.getChildren(), formulas));
        }
        args.add(OtherFuncInstruction.arg(param2, formulas));
        args.add(OtherFuncInstruction.arg(param3, formulas));
        expression.put("args", args);

        return expression;
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(1).getChildren().getFocusNodes().get(0);
        FocusNode param2 = focusNodes.get(3);
        FocusNode param3 = focusNodes.get(5);

        List<AnnotationToken> tokens = new ArrayList<>();
        AnnotationToken token1 = new AnnotationToken();
        token1.value = focusNodes.get(0).getValue();
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = focusNodes.get(0).getBegin();
        token1.end = focusNodes.get(0).getEnd();
        tokens.add(token1);


        if ("<bool-columns>".equals(param1.getValue())) {
            tokens.addAll(BoolColOrBoolFuncColInst.tokens(param1, formulas, amb));
        } else {
            tokens.addAll(BaseBoolFuncInstruction.tokens(param1.getChildren(), formulas, amb));
        }

        AnnotationToken token2 = new AnnotationToken();
        token2.value = focusNodes.get(2).getValue();
        token2.type = Constant.AnnotationTokenType.SYMBOL;
        token2.begin = focusNodes.get(2).getBegin();
        token2.end = focusNodes.get(2).getEnd();
        tokens.add(token2);

        tokens.add(OtherFuncInstruction.token(param2, amb, formulas));

        AnnotationToken token4 = new AnnotationToken();
        token4.value = focusNodes.get(4).getValue();
        token4.type = Constant.AnnotationTokenType.SYMBOL;
        token4.begin = focusNodes.get(4).getBegin();
        token4.end = focusNodes.get(4).getEnd();
        tokens.add(token4);

        tokens.add(OtherFuncInstruction.token(param3, amb, formulas));
        return tokens;
    }


}
