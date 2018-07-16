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
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/25
 * description:
 */
//<standard-deviation-phrase> := standard deviation <number-source-column>;
public class StandardDeviationInstruction {

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

        FocusNode standardNode = focusNodes.get(0);
        FocusNode deviationNode = focusNodes.get(1);
        FocusPhrase numberPhrase = focusNodes.get(2).getChildren();

        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(standardNode.getValue());
        token1.addToken(deviationNode.getValue());
        token1.value = "standard deviation";
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = standardNode.getBegin();
        token1.end = deviationNode.getEnd();
        datas.addToken(token1);

        JSONObject expression = new JSONObject();
        expression.put("name", "standard deviation");
        expression.put("type", "function");
        JSONArray args = new JSONArray();

        JSONObject json = NumberColInstruction.build(numberPhrase, formulas);
        JSONObject arg = new JSONObject();
        arg.put("type", Constant.InstType.COLUMN);
        Column column = (Column) json.get("column");
        arg.put("value", column.getColumnId());

        args.add(arg);
        expression.put("args", args);
        json1.put("expression", expression);
        instructions.add(json1);

        int begin = numberPhrase.getFirstNode().getBegin();
        int end = numberPhrase.getLastNode().getEnd();
        datas.addToken(AnnotationToken.singleCol(column, numberPhrase.size() == 2, begin, end, amb));

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

}