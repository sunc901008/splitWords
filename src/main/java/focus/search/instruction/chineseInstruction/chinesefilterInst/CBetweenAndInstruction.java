package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.filterInst.dateComplexInst.BetweenAndInstruction;
import focus.search.instruction.nodeArgs.ColValueOrDateColInst;
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
 * date: 2018/6/19
 * description:
 */
//<between-and-filter> := <all-date-column> 在 <date-string-value> 和 <date-string-value> 之间的 |
//        在 <date-string-value> 和 <date-string-value> 之间的 |
//        <date-string-value> 和 <date-string-value> 之间的;
public class CBetweenAndInstruction {
    private static final Logger logger = Logger.getLogger(CBetweenAndInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("CBetweenAndInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if (Objects.equals("<all-date-column>", first.getValue())) {
            return build1(focusPhrase, index, amb, formulas);
        } else {
            return build2(focusPhrase, index, amb, formulas, dateColumns);
        }
    }

    //    <all-date-column> 在 <date-string-value> 和 <date-string-value> 之间的
    private static JSONArray build1(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException, AmbiguitiesException {
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

        FocusNode keyword1 = focusNodes.get(1);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(keyword1.getValue());
        token2.value = keyword1.getValue();
        token2.type = Constant.AnnotationCategory.FILTER;
        token2.begin = keyword1.getBegin();
        token2.end = keyword1.getEnd();
        datas.addToken(token2);

        FocusNode param1 = focusNodes.get(2);
        JSONObject arg1 = ColValueOrDateColInst.arg(param1, formulas);
        datas.addTokens(ColValueOrDateColInst.tokens(param1, formulas, amb));

        FocusNode keyword2 = focusNodes.get(3);
        AnnotationToken token4 = new AnnotationToken();
        token4.addToken(keyword2.getValue());
        token4.value = keyword2.getValue();
        token4.type = Constant.AnnotationCategory.FILTER;
        token4.begin = keyword2.getBegin();
        token4.end = keyword2.getEnd();
        datas.addToken(token4);

        FocusNode param2 = focusNodes.get(4);
        JSONObject arg2 = ColValueOrDateColInst.arg(param2, formulas);
        datas.addTokens(ColValueOrDateColInst.tokens(param2, formulas, amb));

        FocusNode keyword3 = focusNodes.get(5);
        AnnotationToken token6 = new AnnotationToken();
        token6.addToken(keyword3.getValue());
        token6.value = keyword3.getValue();
        token6.type = Constant.AnnotationCategory.FILTER;
        token6.begin = keyword3.getBegin();
        token6.end = keyword3.getEnd();
        datas.addToken(token6);

        JSONArray res = BetweenAndInstruction.sort(arg1, arg2);

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

    //    在 <date-string-value> 和 <date-string-value> 之间的
    //    <date-string-value> 和 <date-string-value> 之间的
    private static JSONArray build2(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
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
            AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getByValue("between_and", amb);
            int type = Constant.AmbiguityType.types.indexOf("between_and") - Constant.AmbiguityType.types.size();
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
                ambiguity = AnnotationToken.getAmbiguityDatas(amb, "between_and", title.toString().trim(), focusPhrase.getFirstNode().getBegin(), focusPhrase.getLastNode().getEnd());
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

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        int flag = 0;
        FocusNode first = focusNodes.get(flag++);
        FocusNode param1;
        if (!"<date-string-value>".equals(first.getValue())) {
            param1 = focusNodes.get(flag++);
            AnnotationToken token1 = new AnnotationToken();
            token1.addToken(first.getValue());
            token1.value = first.getValue();
            token1.type = Constant.AnnotationCategory.FILTER;
            token1.begin = first.getBegin();
            token1.end = first.getEnd();
            datas.addToken(token1);
        } else {
            param1 = first;
        }

        JSONObject arg1 = ColValueOrDateColInst.arg(param1, formulas);
        datas.addTokens(ColValueOrDateColInst.tokens(param1, formulas, amb));

        FocusNode keyword1 = focusNodes.get(flag++);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(keyword1.getValue());
        token2.value = keyword1.getValue();
        token2.type = Constant.AnnotationCategory.FILTER;
        token2.begin = keyword1.getBegin();
        token2.end = keyword1.getEnd();
        datas.addToken(token2);

        FocusNode param2 = focusNodes.get(flag++);
        JSONObject arg2 = ColValueOrDateColInst.arg(param2, formulas);
        datas.addTokens(ColValueOrDateColInst.tokens(param2, formulas, amb));

        FocusNode keyword2 = focusNodes.get(flag);
        AnnotationToken token4 = new AnnotationToken();
        token4.description = "column " + dateCol.getColumnDisplayName() + " in " + dateCol.getSourceName();
        token4.tableName = dateCol.getSourceName();
        token4.columnName = dateCol.getColumnDisplayName();
        token4.columnId = dateCol.getColumnId();
        token4.addToken(keyword2.getValue());
        token4.value = keyword2.getValue();
        token4.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token4.begin = keyword2.getBegin();
        token4.end = keyword2.getEnd();
        token4.ambiguity = ambiguity;
        datas.addToken(token4);

        JSONArray res = BetweenAndInstruction.sort(arg1, arg2);

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

}
