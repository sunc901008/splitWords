package focus.search.instruction.functionInst.numberFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.NumberOrNumColInst;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<max-function> := max ( <all-date-column> ) |
//        max ( <number-columns> ) |
//        max ( <number> );

//<min-function> := min ( <all-date-column> ) |
//        min ( <number-columns> ) |
//        min ( <number> );
public class MaxMinFuncInstruction {

    // 完整指令 max / min
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression");

        json1.put("expression", arg(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);
        datas.addTokens(tokens(focusPhrase, formulas, amb));
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException {
        FocusNode param = focusPhrase.getFocusNodes().get(2);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        if ("<all-date-column>".equals(param.getValue())) {
            JSONObject arg1 = new JSONObject();
            JSONObject json = ColumnInstruction.build(param.getChildren());
            arg1.put("type", Constant.InstType.COLUMN);
            arg1.put("column", ((Column) json.get("column")).getColumnId());
            args.add(arg1);
        } else {
            args.add(NumberOrNumColInst.arg(param, formulas));
        }

        expression.put("args", args);

        return expression;
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<AnnotationToken> tokens = new ArrayList<>();
        FocusNode param = focusPhrase.getFocusNodes().get(2);
        AnnotationToken token1 = new AnnotationToken();
        token1.value = focusPhrase.getFocusNodes().get(0).getValue();
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = focusPhrase.getFocusNodes().get(0).getBegin();
        token1.end = focusPhrase.getFocusNodes().get(0).getEnd();
        tokens.add(token1);

        AnnotationToken token2 = new AnnotationToken();
        token2.value = focusPhrase.getFocusNodes().get(1).getValue();
        token2.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token2.begin = focusPhrase.getFocusNodes().get(1).getBegin();
        token2.end = focusPhrase.getFocusNodes().get(1).getEnd();
        tokens.add(token2);

        if ("<all-date-column>".equals(param.getValue())) {
            JSONObject json = ColumnInstruction.build(param.getChildren());
            FocusPhrase third = param.getChildren();
            int begin = third.getFirstNode().getBegin();
            int end = third.getLastNode().getEnd();
            tokens.add(AnnotationToken.singleCol((Column) json.get("column"), third.size() == 2, begin, end, amb));
        } else {
            tokens.addAll(NumberOrNumColInst.tokens(param, formulas, amb));
        }

        AnnotationToken token4 = new AnnotationToken();
        token4.value = focusPhrase.getFocusNodes().get(3).getValue();
        token4.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token4.begin = focusPhrase.getFocusNodes().get(3).getBegin();
        token4.end = focusPhrase.getFocusNodes().get(3).getEnd();
        tokens.add(token4);

        return tokens;
    }

}