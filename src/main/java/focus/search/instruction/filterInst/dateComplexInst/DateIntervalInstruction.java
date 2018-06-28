package focus.search.instruction.filterInst.dateComplexInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.AmbiguityDatas;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/29
 * description:
 */
//<date-interval> := daily |
//        weekly |
//        monthly |
//        quarterly |
//        yearly |
//        by day of week |
//        by week |
//        by month |
//        <to-date-interval>;
public class DateIntervalInstruction {
    private static final Logger logger = Logger.getLogger(DateIntervalInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("DateIntervalInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();

        FocusNode fn = focusNodes.get(0);
        if ("<to-date-interval>".equals(fn.getValue())) {
            return buildToDate(fn.getChildren(), index, amb, dateColumns);
        }
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.DATETIME_INTERVAL);

        json1.put("interval", fn.getValue());

        JSONObject json = CommonFunc.checkAmb(focusPhrase, fn, dateColumns, amb, "date_interval");
        AmbiguityDatas ambiguity = (AmbiguityDatas) json.get("ambiguity");
        Column dateCol = (Column) json.get("column");

        json1.put("column", dateCol.getColumnId());

        AnnotationToken token1 = new AnnotationToken();
        token1.description = "column " + dateCol.getColumnDisplayName() + " in " + dateCol.getSourceName();
        token1.tableName = dateCol.getSourceName();
        token1.columnName = dateCol.getColumnDisplayName();
        token1.columnId = dateCol.getColumnId();
        token1.addToken(fn.getValue());
        token1.value = fn.getValue();
        token1.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token1.begin = fn.getBegin();
        token1.end = fn.getEnd();
        token1.ambiguity = ambiguity;
        datas.addToken(token1);

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < focusNodes.size(); i++) {
            FocusNode tmp = focusNodes.get(i);
            AnnotationToken token = new AnnotationToken();
            token.addToken(tmp.getValue());
            token.value = tmp.getValue();
            token.begin = tmp.getBegin();
            token.end = tmp.getEnd();
            datas.addToken(token);
            sb.append(tmp.getValue()).append(" ");
        }

        if (sb.length() > 0) {
            json1.put("interval", sb.substring(0, sb.length() - 1));
        }

        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

    //<to-date-interval> := today |
    //    week to date |
    //    month to date |
    //    quarter to date |
    //    year to date;
    private static JSONArray buildToDate(FocusPhrase focusPhrase, int index, JSONObject amb, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        String key = focusPhrase.getFirstNode().getValue();
        return buildToDate(focusPhrase, index, amb, dateColumns, key);
    }

    public static JSONArray buildToDate(FocusPhrase focusPhrase, int index, JSONObject amb, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode fn = focusNodes.get(0);
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        JSONObject json = CommonFunc.checkAmb(focusPhrase, focusPhrase.getFirstNode(), dateColumns, amb, "date_interval");
        AmbiguityDatas ambiguity = (AmbiguityDatas) json.get("ambiguity");
        Column dateCol = (Column) json.get("column");

        AnnotationToken token1 = new AnnotationToken();
        token1.description = "column " + dateCol.getColumnDisplayName() + " in " + dateCol.getSourceName();
        token1.tableName = dateCol.getSourceName();
        token1.columnName = dateCol.getColumnDisplayName();
        token1.columnId = dateCol.getColumnId();
        token1.addToken(fn.getValue());
        token1.value = fn.getValue();
        token1.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token1.begin = fn.getBegin();
        token1.end = fn.getEnd();
        token1.ambiguity = ambiguity;
        datas.addToken(token1);

        for (int i = 1; i < focusNodes.size(); i++) {
            FocusNode node = focusNodes.get(i);
            AnnotationToken token = new AnnotationToken();
            token.addToken(node.getValue());
            token.value = node.getValue();
            token.begin = node.getBegin();
            token.end = node.getEnd();
            datas.addToken(token);
        }

        List<String> params = CommonFunc.params(key);

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
