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
 * date: 2018/5/28
 * description:
 */
//<average-phrase> := <number-source-column> 的平均值 |
//        平均 <number-source-column>;
public class CAverageInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if ("<number-source-column>".equals(first.getValue())) {
            return build1(focusPhrase, index, amb, formulas);
        } else {
            return build2(focusPhrase, index, amb, formulas);
        }
    }

    //    <number-source-column> 的平均值
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


        json1.put("aggregation", Constant.AggregationType.AVERAGE);

        json1.put("expression", NumberColInstruction.arg(numberPhrase, formulas));
        instructions.add(json1);

        datas.addToken(AnnotationToken.singleCol(numberPhrase, amb, formulas));

        FocusNode averageNode = focusNodes.get(1);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(averageNode.getValue());
        token2.value = averageNode.getValue();
        token2.type = Constant.AnnotationTokenType.SYMBOL;
        token2.begin = averageNode.getBegin();
        token2.end = averageNode.getEnd();
        datas.addToken(token2);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

    //    平均 <number-source-column>
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

        FocusNode averageNode = focusNodes.get(0);
        FocusPhrase numberPhrase = focusNodes.get(1).getChildren();

        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(averageNode.getValue());
        token1.value = averageNode.getValue();
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = averageNode.getBegin();
        token1.end = averageNode.getEnd();
        datas.addToken(token1);

        json1.put("aggregation", Constant.AggregationType.AVERAGE);

        json1.put("expression", NumberColInstruction.arg(numberPhrase, formulas));
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
