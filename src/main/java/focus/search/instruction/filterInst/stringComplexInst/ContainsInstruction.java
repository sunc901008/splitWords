package focus.search.instruction.filterInst.stringComplexInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.ColumnInstruction;
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
//<contains-filter> := <all-string-column> contains <column-value>;
public class ContainsInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
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

        FocusNode contains = focusNodes.get(1);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(contains.getValue());
        token2.value = contains.getValue();
        token2.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token2.begin = contains.getBegin();
        token2.end = contains.getEnd();
        datas.addToken(token2);

        datas.addToken(ColumnValueInstruction.token(valueNode));

        JSONObject expression = new JSONObject();
        expression.put("name", "contains");
        expression.put("type", "function");
        JSONArray args = new JSONArray();
        args.add(ColumnInstruction.arg(stringPhrase));
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