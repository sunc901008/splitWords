package focus.search.instruction.filterInst.dateComplexInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.ColValueOrDateColInst;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.instruction.sourceInst.NumberColInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/19
 * description:
 */
//<between-and-filter> := <all-date-column> between <date-string-value> and <date-string-value> |
//        <number-source-column> between <number> and <number>;
public class BetweenAndInstruction {
    private static final Logger logger = Logger.getLogger(BetweenAndInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("BetweenAndInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if ("<all-date-column>".equals(first.getValue())) {
            return build1(focusPhrase, index, amb, formulas);
        }
        return build2(focusPhrase, index, amb, formulas);
    }

    //    <all-date-column> between <date-string-value> and <date-string-value>
    public static JSONArray build1(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusPhrase datePhrase = focusNodes.get(0).getChildren();
        Column dateCol = datePhrase.getLastNode().getColumn();
        datas.addToken(AnnotationToken.singleCol(datePhrase, amb));

        AnnotationToken token2 = new AnnotationToken();
        token2.addToken("between");
        token2.value = "between";
        token2.type = Constant.AnnotationCategory.FILTER;
        token2.begin = focusNodes.get(1).getBegin();
        token2.end = focusNodes.get(1).getEnd();
        datas.addToken(token2);

        FocusNode param1 = focusNodes.get(2);
        JSONObject arg1 = ColValueOrDateColInst.arg(param1, formulas);
        datas.addTokens(ColValueOrDateColInst.tokens(param1, formulas, amb));

        AnnotationToken token4 = new AnnotationToken();
        token4.addToken("and");
        token4.value = "and";
        token4.type = Constant.AnnotationCategory.FILTER;
        token4.begin = focusNodes.get(3).getBegin();
        token4.end = focusNodes.get(3).getEnd();
        datas.addToken(token4);

        FocusNode param2 = focusNodes.get(4);
        JSONObject arg2 = ColValueOrDateColInst.arg(param2, formulas);
        datas.addTokens(ColValueOrDateColInst.tokens(param2, formulas, amb));

        JSONArray res = sort(arg1, arg2);

        JSONObject expressionStart = new JSONObject();
        expressionStart.put("name", ">=");
        expressionStart.put("type", Constant.InstType.FUNCTION);
        JSONArray argStarts = new JSONArray();
        JSONObject argStart1 = new JSONObject();
        argStart1.put("type", Constant.InstType.COLUMN);
        argStart1.put("value", dateCol.getColumnId());
        argStarts.add(argStart1);
        JSONObject argStart2 = new JSONObject();
        argStart2.put("type", Constant.InstType.DATE);
        argStart2.put("value", res.get(0));
        argStarts.add(argStart2);
        expressionStart.put("args", argStarts);

        JSONObject expressionEnd = new JSONObject();
        expressionEnd.put("name", "<");
        expressionEnd.put("type", Constant.InstType.FUNCTION);
        JSONArray argEnds = new JSONArray();
        JSONObject argEnd1 = new JSONObject();
        argEnd1.put("type", Constant.InstType.COLUMN);
        argEnd1.put("value", dateCol.getColumnId());
        argEnds.add(argEnd1);
        JSONObject argEnd2 = new JSONObject();
        argEnd2.put("type", Constant.InstType.DATE);
        argEnd2.put("value", res.get(1));
        argEnds.add(argEnd2);
        expressionEnd.put("args", argEnds);

        jsonStart.put("expression", expressionStart);

        instructions.add(jsonStart);

        JSONObject jsonEnd = JSONObject.parseObject(jsonStart.toJSONString());
        jsonEnd.put("expression", expressionEnd);
        instructions.add(jsonEnd);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

    //    <number-source-column> between <number> and <number>
    public static JSONArray build2(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusPhrase numberPhrase = focusNodes.get(0).getChildren();
        JSONObject json = NumberColInstruction.build(numberPhrase, formulas);
        Column column = (Column) json.get("column");
        int begin = numberPhrase.getFirstNode().getBegin();
        int end = numberPhrase.getLastNode().getEnd();
        datas.addToken(AnnotationToken.singleCol(column, numberPhrase.size() == 2, begin, end, amb));

        FocusNode between = focusNodes.get(1);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken("between");
        token2.value = "between";
        token2.type = Constant.AnnotationCategory.FILTER;
        token2.begin = between.getBegin();
        token2.end = between.getEnd();
        datas.addToken(token2);

        FocusNode param1 = focusNodes.get(2).getChildren().getFirstNode();
        datas.addToken(NumberArg.token(param1));

        FocusNode and = focusNodes.get(3);
        AnnotationToken token4 = new AnnotationToken();
        token4.addToken("and");
        token4.value = "and";
        token4.type = Constant.AnnotationCategory.FILTER;
        token4.begin = and.getBegin();
        token4.end = and.getEnd();
        datas.addToken(token4);

        FocusNode param2 = focusNodes.get(4).getChildren().getFirstNode();
        datas.addToken(NumberArg.token(param1));

        JSONObject arg1 = NumberArg.arg(param1);
        JSONObject arg2 = NumberArg.arg(param2);
        JSONArray res = sort(arg1, arg2);

        JSONObject expressionStart = new JSONObject();
        expressionStart.put("name", ">=");
        expressionStart.put("type", Constant.InstType.FUNCTION);
        JSONArray argStarts = new JSONArray();
        JSONObject argStart1 = new JSONObject();
        argStart1.put("type", Constant.InstType.COLUMN);
        argStart1.put("value", column.getColumnId());
        argStarts.add(argStart1);
        JSONObject argStart2 = new JSONObject();
        argStart2.put("type", Constant.InstType.NUMBER);
        argStart2.put("value", res.get(0));
        argStarts.add(argStart2);
        expressionStart.put("args", argStarts);

        JSONObject expressionEnd = new JSONObject();
        expressionEnd.put("name", "<");
        expressionEnd.put("type", Constant.InstType.FUNCTION);
        JSONArray argEnds = new JSONArray();
        JSONObject argEnd1 = new JSONObject();
        argEnd1.put("type", Constant.InstType.COLUMN);
        argEnd1.put("value", column.getColumnId());
        argEnds.add(argEnd1);
        JSONObject argEnd2 = new JSONObject();
        argEnd2.put("type", Constant.InstType.NUMBER);
        argEnd2.put("value", res.get(1));
        argEnds.add(argEnd2);
        expressionEnd.put("args", argEnds);

        jsonStart.put("expression", expressionStart);

        instructions.add(jsonStart);

        JSONObject jsonEnd = JSONObject.parseObject(jsonStart.toJSONString());
        jsonEnd.put("expression", expressionEnd);
        instructions.add(jsonEnd);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

    // 日期|数字从小到大排序
    public static JSONArray sort(JSONObject arg1, JSONObject arg2) {
        JSONArray res = new JSONArray();
        String value1 = arg1.getString("value");
        String value2 = arg2.getString("value");
        res.add(value1);
        if (value1.startsWith("-")) {
            if (value2.startsWith("-")) {
                if (value1.compareTo(value2) < 0) {
                    res.add(0, value2);
                    return res;
                }
            }
            res.add(value2);
        } else {
            if (!value2.startsWith("-")) {
                if (value1.compareTo(value2) < 0) {
                    res.add(value2);
                    return res;
                }
            }
            res.add(0, value2);
        }
        return res;
    }

}
