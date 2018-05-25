package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.meta.AmbiguitiesRecord;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.AmbiguityDatas;
import focus.search.response.search.IllegalDatas;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * creator: sunc
 * date: 2018/5/23
 * description:
 */
//<last-filter> := <last-days-filter> |
//        <last-weeks-filter> |
//        <last-months-filter> |
//        <last-quarters-filter> |
//        <last-years-filter>;
public class CLastInstruction {
    private static final Logger logger = Logger.getLogger(CLastInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws
            FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("LastInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<last-days-filter>":
                return CLastDaysInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<last-weeks-filter>":
                return CLastWeeksInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<last-months-filter>":
                return CLastMonthsInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<last-quarters-filter>":
                return CLastQuartersInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<last-years-filter>":
                return CLastYearsInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    //<last-days-chinese> := 天 |
    //        天的;
    //<last-days-filter> := <last-chinese> <integer> <last-days-chinese> |
    //        <all-date-column> <last-chinese> <integer> <last-days-chinese>;
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if (Objects.equals("<all-date-column>", first.getValue())) {
            return buildStartsWithCol(focusPhrase, index, amb, formulas, dateColumns, key);
        } else {
            return buildNoCol(focusPhrase, index, amb, formulas, dateColumns, key);
        }

    }

    //<all-date-column> <last-chinese> <integer> <last-days-chinese>;
    private static JSONArray buildStartsWithCol(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusPhrase datePhrase = focusNodes.get(0).getChildren();
        FocusNode last = focusNodes.get(1).getChildren().getFirstNode();
        Column dateCol = datePhrase.getLastNode().getColumn();

        datas.addToken(AnnotationToken.singleCol(datePhrase, amb));

        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(last.getValue());
        token2.value = last.getValue();
        token2.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token2.begin = last.getBegin();
        token2.end = last.getEnd();
        datas.addToken(token2);

        FocusNode param = focusNodes.get(2);
        param = param.isHasChild() ? param.getChildren().getFirstNode() : param;
        int integer = Integer.parseInt(param.getValue());
        datas.addToken(NumberArg.token(param));

        FocusNode keywordNode = focusPhrase.getLastNode();
        AnnotationToken token4 = new AnnotationToken();
        token4.addToken(keywordNode.getValue());
        token4.value = keywordNode.getValue();
        token4.type = Constant.AnnotationCategory.FILTER;
        token4.begin = keywordNode.getBegin();
        token4.end = keywordNode.getEnd();
        datas.addToken(token4);

        List<String> params = CommonFunc.params(key, integer);

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

    //<last-chinese> <integer> <last-days-chinese>
    private static JSONArray buildNoCol(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusNode last = focusNodes.get(0).getChildren().getFirstNode();
        ;

        Column dateCol;
        AmbiguityDatas ambiguity = null;
        if (dateColumns.size() == 0) {
            // 没有日期列
            String reason = "no date columns in current sources";
            IllegalDatas illegalDatas = new IllegalDatas(focusPhrase.getFirstNode().getBegin(), focusPhrase.getLastNode().getEnd(), reason);
            throw new IllegalException(reason, illegalDatas);
        } else if (dateColumns.size() > 1) {
            // 多个日期列
            // 检测歧义是否解决
            AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getByValue("last", amb);
            int type = Constant.AmbiguityType.types.indexOf("last") - Constant.AmbiguityType.types.size();
            if (ambiguitiesResolve != null && ambiguitiesResolve.isResolved) {// 歧义已经解决过，应用下发
                AmbiguitiesRecord resolve = ambiguitiesResolve.ars.get(0);
                dateCol = new Column();
                dateCol.setColumnDisplayName(resolve.columnName);
                dateCol.setColumnId(resolve.columnId);
                dateCol.setColumnName(resolve.columnName);
                dateCol.setSourceName(resolve.sourceName);
                StringBuilder title = new StringBuilder();
                for (int i = 0; i < focusPhrase.size(); i++) {
                    title.append(focusPhrase.getNodeNew(i).getValue()).append(" ");
                }
                ambiguity = AnnotationToken.getAmbiguityDatas(amb, "last", title.toString().trim(), focusPhrase.getFirstNode().getBegin(), focusPhrase.getLastNode().getEnd());
            } else {// 歧义没有解决过， 返回歧义
                List<AmbiguitiesRecord> ars = new ArrayList<>();
                for (Column col : dateColumns) {
                    AmbiguitiesRecord ar = new AmbiguitiesRecord();
                    ar.type = Constant.AmbiguityType.COLUMN;
                    ar.sourceName = col.getSourceName();
                    ar.columnId = col.getColumnId();
                    ar.columnName = col.getColumnDisplayName();
                    ar.realValue = ar.columnName;
                    ar.possibleValue = ar.columnName;
                    ars.add(ar);
                }
                throw new AmbiguitiesException(ars, focusPhrase.getFirstNode().getBegin(), focusPhrase.getLastNode().getEnd(), type);
            }
        } else {
            dateCol = dateColumns.get(0);
        }

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

        FocusNode param = focusNodes.get(1);
        param = param.isHasChild() ? param.getChildren().getFirstNode() : param;
        int integer = Integer.parseInt(param.getValue());
        datas.addToken(NumberArg.token(param));

        FocusNode keywordNode = focusPhrase.getLastNode();
        AnnotationToken token3 = new AnnotationToken();
        token3.addToken(keywordNode.getValue());
        token3.value = keywordNode.getValue();
        token3.type = Constant.AnnotationCategory.FILTER;
        token3.begin = keywordNode.getBegin();
        token3.end = keywordNode.getEnd();
        datas.addToken(token3);

        List<String> params = CommonFunc.params(key, integer);

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
