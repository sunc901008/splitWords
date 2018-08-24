package focus.search.instruction.phraseInst.numberComplexInst;

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
//<unique-count-phrase> := unique count <number-source-column>;
public class UniqueCountInstruction {

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

        FocusNode uniqueNode = focusNodes.get(0);
        FocusNode countNode = focusNodes.get(1);
        FocusPhrase numberPhrase = focusNodes.get(2).getChildren();

        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(uniqueNode.getValue());
        token1.addToken(countNode.getValue());
        token1.value = "unique count";
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = uniqueNode.getBegin();
        token1.end = countNode.getEnd();
        datas.addToken(token1);

        json1.put("aggregation", Constant.AggregationType.COUNT_DISTINCT);

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
