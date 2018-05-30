package focus.search.instruction.filterInst.stringComplexInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.Base;
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
//<not-begins-with-filter> := <all-string-column> not begins with <column-value>;
public class NotBeginsWithInstruction {

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
        FocusNode valueNode = focusNodes.get(4);

        datas.addToken(AnnotationToken.singleCol(stringPhrase, amb));

        FocusNode not = focusNodes.get(1);
        FocusNode begins = focusNodes.get(2);
        FocusNode with = focusNodes.get(3);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(not.getValue());
        token2.addToken(begins.getValue());
        token2.addToken(with.getValue());
        String value = not.getValue() + Base.space(begins.getBegin() - not.getEnd()) + begins.getValue();
        value = value + Base.space(with.getBegin() - begins.getEnd()) + with.getValue();
        token2.value = value;
        token2.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token2.begin = not.getBegin();
        token2.end = with.getEnd();
        datas.addToken(token2);

        datas.addToken(ColumnValueInstruction.token(valueNode));

        JSONObject expression = new JSONObject();
        expression.put("name", "not begins with");
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
