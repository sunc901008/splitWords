package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.instruction.sourceInst.StringColInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/28
 * description:
 */
//<not-begins-with-filter> := <all-string-column> 开头不是 <column-value> |
//        开头不是 <column-value> 的 <all-string-column>;
public class CNotBeginsWithInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if ("<all-string-column>".equals(first.getValue())) {
            return build1(focusPhrase, index, amb, formulas);
        } else {
            return build2(focusPhrase, index, amb, formulas);
        }
    }

    //    <all-string-column> 开头是 <column-value>
    private static JSONArray build1(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusPhrase stringPhrase = focusNodes.get(0).getChildren();
        FocusNode valueNode = focusNodes.get(2);

        datas.addToken(AnnotationToken.singleCol(stringPhrase, amb));

        FocusNode key = focusNodes.get(1);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(key.getValue());
        token2.value = key.getValue();
        token2.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token2.begin = key.getBegin();
        token2.end = key.getEnd();
        datas.addToken(token2);

        datas.addToken(ColumnValueInstruction.token(valueNode));

        JSONObject expression = new JSONObject();
        expression.put("name", "not begins with");
        expression.put("type", "function");
        JSONArray args = new JSONArray();
        args.add(StringColInstruction.arg(stringPhrase, formulas));
        args.add(ColumnValueInstruction.arg(valueNode));
        expression.put("args", args);
        json1.put("expression", expression);
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

    //    开头是 <column-value> 的 <all-string-column>;
    private static JSONArray build2(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusNode key1 = focusNodes.get(0);
        FocusPhrase stringPhrase = focusNodes.get(1).getChildren();
        FocusNode key2 = focusNodes.get(2);
        FocusNode valueNode = focusNodes.get(3);

        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(key1.getValue());
        token1.value = key1.getValue();
        token1.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token1.begin = key1.getBegin();
        token1.end = key1.getEnd();
        datas.addToken(token1);

        datas.addToken(AnnotationToken.singleCol(stringPhrase, amb));

        AnnotationToken token3 = new AnnotationToken();
        token3.addToken(key2.getValue());
        token3.value = key2.getValue();
        token3.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token3.begin = key2.getBegin();
        token3.end = key2.getEnd();
        datas.addToken(token3);

        datas.addToken(ColumnValueInstruction.token(valueNode));

        JSONObject expression = new JSONObject();
        expression.put("name", "not begins with");
        expression.put("type", "function");
        JSONArray args = new JSONArray();
        args.add(StringColInstruction.arg(stringPhrase, formulas));
        args.add(ColumnValueInstruction.arg(valueNode));
        expression.put("args", args);
        json1.put("expression", expression);
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

}
