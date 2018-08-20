package focus.search.instruction.filterInst.dateComplexInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.AmbiguityDatas;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * creator: sunc
 * date: 2018/6/26
 * description:
 */
//<next-filter> := <next-days-filter> |
//        <next-weeks-filter> |
//        <next-months-filter> |
//        <next-quarters-filter> |
//        <next-years-filter> |
//        <next-minutes-filter> |
//        <next-hours-filter>;
public class NextInstruction {
    private static final Logger logger = Logger.getLogger(NextInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws
            FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("NextInstruction instruction arg. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<next-days-filter>":
                return build(fn.getChildren(), index, amb, formulas, dateColumns, "day");
            case "<next-weeks-filter>":
                return build(fn.getChildren(), index, amb, formulas, dateColumns, "week");
            case "<next-months-filter>":
                return build(fn.getChildren(), index, amb, formulas, dateColumns, "month");
            case "<next-quarters-filter>":
                return build(fn.getChildren(), index, amb, formulas, dateColumns, "quarter");
            case "<next-years-filter>":
                return build(fn.getChildren(), index, amb, formulas, dateColumns, "year");
            case "<next-minutes-filter>":
                return build(fn.getChildren(), index, amb, formulas, dateColumns, "minute");
            case "<next-hours-filter>":
                return build(fn.getChildren(), index, amb, formulas, dateColumns, "hour");
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    //<next-days-filter> := next day |
    //                      <all-date-column> next day |
    //                      next <integer> days |
    //                      <all-date-column> next <integer> days;
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if (Objects.equals("<all-date-column>", first.getValue())) {
            return buildStartsWithCol(focusPhrase, index, amb, formulas, key);
        } else {
            return buildNoCol(focusPhrase, index, amb, dateColumns, key);
        }

    }

    //<all-date-column> next day
    //<all-date-column> next <integer> days
    private static JSONArray buildStartsWithCol(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusPhrase datePhrase = focusNodes.get(0).getChildren();
        FocusNode last = focusNodes.get(1);
        Column dateCol = datePhrase.getLastNode().getColumn();

        datas.addToken(AnnotationToken.singleCol(datePhrase, amb, formulas));

        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(last.getValue());
        token2.value = last.getValue();
        token2.type = Constant.AnnotationCategory.FILTER;
        token2.begin = last.getBegin();
        token2.end = last.getEnd();
        datas.addToken(token2);

        FocusNode keywordNode = focusPhrase.getLastNode();
        int integer = 1;
        if (keywordNode.getValue().endsWith("s")) {
            FocusNode param = focusNodes.get(2);
            param = param.isHasChild() ? param.getChildren().getFirstNode() : param;
            integer = Integer.parseInt(param.getValue());
            datas.addToken(NumberArg.token(param));
        }

        AnnotationToken token4 = new AnnotationToken();
        token4.addToken(keywordNode.getValue());
        token4.value = keywordNode.getValue();
        token4.type = Constant.AnnotationCategory.FILTER;
        token4.begin = keywordNode.getBegin();
        token4.end = keywordNode.getEnd();
        datas.addToken(token4);

        List<String> params = CommonFunc.nextParams(key, integer);

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
        argStart2.put("value", params.get(0));
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
        argEnd2.put("value", params.get(1));
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

    //next day
    //next <integer> days
    private static JSONArray buildNoCol(FocusPhrase focusPhrase, int index, JSONObject amb, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusNode last = focusNodes.get(0);

        JSONObject json = CommonFunc.checkAmb(focusPhrase, focusPhrase.getFirstNode(), dateColumns, amb, "last");
        AmbiguityDatas ambiguity = (AmbiguityDatas) json.get("ambiguity");
        Column dateCol = (Column) json.get("column");

        AnnotationToken token1 = new AnnotationToken();
        token1.description = "column " + dateCol.getColumnDisplayName() + " in " + dateCol.getSourceName();
        token1.tableName = dateCol.getSourceName();
        token1.columnName = dateCol.getColumnDisplayName();
        token1.columnId = dateCol.getColumnId();
        token1.addToken(last.getValue());
        token1.value = last.getValue();
        token1.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token1.begin = last.getBegin();
        token1.end = last.getEnd();
        token1.ambiguity = ambiguity;
        datas.addToken(token1);

        FocusNode keywordNode = focusPhrase.getLastNode();
        int integer = 1;
        if (keywordNode.getValue().endsWith("s")) {
            FocusNode param = focusNodes.get(1);
            param = param.isHasChild() ? param.getChildren().getFirstNode() : param;
            integer = Integer.parseInt(param.getValue());
            datas.addToken(NumberArg.token(param));
        }

        AnnotationToken token3 = new AnnotationToken();
        token3.addToken(keywordNode.getValue());
        token3.value = keywordNode.getValue();
        token3.type = Constant.AnnotationCategory.FILTER;
        token3.begin = keywordNode.getBegin();
        token3.end = keywordNode.getEnd();
        datas.addToken(token3);

        List<String> params = CommonFunc.nextParams(key, integer);

        JSONObject expressionStart = new JSONObject();
        expressionStart.put("name", ">=");
        expressionStart.put("type", "function");
        JSONArray argStarts = new JSONArray();
        JSONObject argStart1 = new JSONObject();
        argStart1.put("type", Constant.InstType.COLUMN);
        argStart1.put("value", dateCol.getColumnId());
        argStarts.add(argStart1);
        JSONObject argStart2 = new JSONObject();
        argStart2.put("type", Constant.InstType.DATE);
        argStart2.put("value", params.get(0));
        argStarts.add(argStart2);
        expressionStart.put("args", argStarts);

        JSONObject expressionEnd = new JSONObject();
        expressionEnd.put("name", "<");
        expressionEnd.put("type", "function");
        JSONArray argEnds = new JSONArray();
        JSONObject argEnd1 = new JSONObject();
        argEnd1.put("type", Constant.InstType.COLUMN);
        argEnd1.put("value", dateCol.getColumnId());
        argEnds.add(argEnd1);
        JSONObject argEnd2 = new JSONObject();
        argEnd2.put("type", Constant.InstType.DATE);
        argEnd2.put("value", params.get(1));
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

}
