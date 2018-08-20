package focus.search.instruction.chineseInstruction.chinesephraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.Base;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.NumberColInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/25
 * description:
 */
//<sum-chinese> := 总量 |
//        总和 |
//        总数;
//<sum-1-chinese> := 的总量 |
//        的总和 |
//        的总数;
//<sum-phrase> := <number-source-column> <sum-1-chinese> |
//        <sum-chinese> <number-source-column>;
public class CSumInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if ("<number-source-column>".equals(first.getValue())) {
            return build1(focusPhrase, index, amb, formulas);
        } else {
            return build2(focusPhrase, index, amb, formulas);
        }
    }

    //    <number-source-column> <sum-1-chinese>
    private static JSONArray build1(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_EXPRESSION);
        json1.put("category", Constant.AnnotationCategory.EXPRESSION);
        json1.put("name", Base.InstName(focusPhrase));
        json1.put("type", Constant.ColumnType.MEASURE);

        FocusPhrase numberPhrase = focusNodes.get(0).getChildren();

        JSONObject expression = new JSONObject();
        expression.put("name", "sum");
        expression.put("type", "function");
        JSONArray args = new JSONArray();

        args.add(NumberColInstruction.arg(numberPhrase, formulas));
        expression.put("args", args);
        json1.put("expression", expression);
        instructions.add(json1);

        datas.addToken(AnnotationToken.singleCol(numberPhrase, amb, formulas));

        FocusNode sumNode = focusNodes.get(1);
        sumNode = sumNode.isHasChild() ? sumNode.getChildren().getFirstNode() : sumNode;
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(sumNode.getValue());
        token2.value = sumNode.getValue();
        token2.type = Constant.AnnotationTokenType.SYMBOL;
        token2.begin = sumNode.getBegin();
        token2.end = sumNode.getEnd();
        datas.addToken(token2);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

    //    <sum-chinese> <number-source-column>;
    private static JSONArray build2(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_EXPRESSION);
        json1.put("category", Constant.AnnotationCategory.EXPRESSION);
        json1.put("name", Base.InstName(focusPhrase));
        json1.put("type", Constant.ColumnType.MEASURE);

        FocusNode sumNode = focusNodes.get(0);
        sumNode = sumNode.isHasChild() ? sumNode.getChildren().getFirstNode() : sumNode;
        FocusPhrase numberPhrase = focusNodes.get(1).getChildren();

        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(sumNode.getValue());
        token1.value = sumNode.getValue();
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = sumNode.getBegin();
        token1.end = sumNode.getEnd();
        datas.addToken(token1);

        JSONObject expression = new JSONObject();
        expression.put("name", "sum");
        expression.put("type", "function");
        JSONArray args = new JSONArray();

        args.add(NumberColInstruction.arg(numberPhrase, formulas));
        expression.put("args", args);
        json1.put("expression", expression);
        instructions.add(json1);

        datas.addToken(AnnotationToken.singleCol(numberPhrase, amb, formulas));

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }


}
