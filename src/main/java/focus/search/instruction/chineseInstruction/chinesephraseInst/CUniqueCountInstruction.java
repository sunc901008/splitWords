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
//<unique-count-chinese> := 去重后的数量 |
//        的去重后的数量;
//<unique-count-phrase> := <number-source-column> <unique-count-chinese>;
public class CUniqueCountInstruction {

    //    <number-source-column> <unique-count-chinese>
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
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

        json1.put("aggregation", Constant.AggregationType.COUNT_DISTINCT);

        json1.put("expression", NumberColInstruction.arg(numberPhrase, formulas));
        instructions.add(json1);

        datas.addToken(AnnotationToken.singleCol(numberPhrase, amb, formulas));

        FocusNode keywordNode = focusNodes.get(1);
        keywordNode = keywordNode.isHasChild() ? keywordNode.getChildren().getFirstNode() : keywordNode;
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(keywordNode.getValue());
        token2.value = keywordNode.getValue();
        token2.type = Constant.AnnotationTokenType.SYMBOL;
        token2.begin = keywordNode.getBegin();
        token2.end = keywordNode.getEnd();
        datas.addToken(token2);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

}
