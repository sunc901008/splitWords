package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.Base;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.filterInst.dateComplexInst.BetweenAndInstruction;
import focus.search.instruction.nodeArgs.ColValueOrDateColInst;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.instruction.sourceInst.NumberColInstruction;
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
 * date: 2018/6/19
 * description:
 */
//<between-and-filter> := <all-date-column> 在 <date-string-value> 和 <date-string-value> 之间的 |
//        在 <date-string-value> 和 <date-string-value> 之间的 |
//        <date-string-value> 和 <date-string-value> 之间的 |
//        <number-source-column> 在 <date-string-value> 和 <date-string-value> 之间 |
//        <between-and-1-filter>;
public class CBetweenAndInstruction {
    private static final Logger logger = Logger.getLogger(CBetweenAndInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("CBetweenAndInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if (Objects.equals("<all-date-column>", first.getValue())) {
            return build1(focusPhrase, index, amb, formulas);
        } else if (Objects.equals("<between-and-1-filter>", first.getValue())) {
            return build3(first.getChildren(), index, amb, formulas);
        } else if (Objects.equals("<number-source-column>", first.getValue())) {
            return build4(focusPhrase, index, amb, formulas, dateColumns);
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

        JSONObject json = CommonFunc.checkAmb(focusPhrase, focusPhrase.getFirstNode(), dateColumns, amb, "between_and");
        AmbiguityDatas ambiguity = (AmbiguityDatas) json.get("ambiguity");
        Column dateCol = (Column) json.get("column");

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

    //    <between-and-1-filter> := <number-source-column> 在 <number> 和 <number> 之间 |
    //    在 <number> 和 <number> 之间的 <number-source-column>;
    private static JSONArray build3(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException,
            IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        int flag = 0;
        boolean atLast = false;
        FocusNode first = focusNodes.get(flag);
        if (first.isTerminal()) {
            atLast = true;
        } else {
            flag++;
        }

        FocusNode keyword1 = focusNodes.get(flag++);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(keyword1.getValue());
        token2.value = keyword1.getValue();
        token2.type = Constant.AnnotationCategory.FILTER;
        token2.begin = keyword1.getBegin();
        token2.end = keyword1.getEnd();
        datas.addToken(token2);

        FocusNode param1 = focusNodes.get(flag++);
        JSONObject arg1 = NumberArg.arg(param1);
        datas.addToken(NumberArg.token(param1));

        FocusNode keyword2 = focusNodes.get(flag++);
        AnnotationToken token4 = new AnnotationToken();
        token4.addToken(keyword2.getValue());
        token4.value = keyword2.getValue();
        token4.type = Constant.AnnotationCategory.FILTER;
        token4.begin = keyword2.getBegin();
        token4.end = keyword2.getEnd();
        datas.addToken(token4);

        FocusNode param2 = focusNodes.get(flag++);
        JSONObject arg2 = NumberArg.arg(param2);
        datas.addToken(NumberArg.token(param2));

        FocusNode keyword3 = focusNodes.get(flag++);
        AnnotationToken token6 = new AnnotationToken();
        token6.addToken(keyword3.getValue());
        token6.value = keyword3.getValue();
        token6.type = Constant.AnnotationCategory.FILTER;
        token6.begin = keyword3.getBegin();
        token6.end = keyword3.getEnd();
        datas.addToken(token6);

        FocusPhrase numberPhrase;
        if (atLast) {
            numberPhrase = focusNodes.get(flag).getChildren();
        } else {
            numberPhrase = first.getChildren();
        }
        JSONObject json = NumberColInstruction.build(numberPhrase, formulas);
        Column column = (Column) json.get("column");
        int begin = numberPhrase.getFirstNode().getBegin();
        int end = numberPhrase.getLastNode().getEnd();
        if (atLast) {
            datas.addToken(AnnotationToken.singleCol(column, numberPhrase.size() == 2, begin, end, amb));
        } else {
            datas.addToken(0, AnnotationToken.singleCol(column, numberPhrase.size() == 2, begin, end, amb));
        }
        JSONArray res = BetweenAndInstruction.sort(arg1, arg2);

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

    //        <number-source-column> 在 <date-string-value> 和 <date-string-value> 之间 |
    private static JSONArray build4(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException,
            IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();

        FocusNode numberSourceColumn = focusNodes.get(0);
        FocusPhrase numberSourcePhrase = numberSourceColumn.getChildren();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_EXPRESSION);
        json1.put("category", Constant.AnnotationCategory.EXPRESSION);

        json1.put("name", Base.InstName(numberSourcePhrase));

        json1.put("expression", ColumnInstruction.arg(numberSourcePhrase));
        instructions.add(json1);
        logger.debug(instructions);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);

        // annotation content
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE);

        if ("<number-function-column>".equals(focusPhrase.getFocusNodes().get(0).getValue())) {
            datas.category = Constant.AnnotationCategory.EXPRESSION;
        } else {
            datas.category = Constant.AnnotationCategory.MEASURE_COLUMN;
        }

        datas.addToken(AnnotationToken.singleCol(numberSourcePhrase, amb));
        json2.put("content", datas);

        instructions.add(json2);
        //----------------------------------------
        JSONObject json = CommonFunc.checkAmb(focusPhrase, focusPhrase.getFirstNode(), dateColumns, amb, "between_and");
        AmbiguityDatas ambiguity = (AmbiguityDatas) json.get("ambiguity");
        Column dateCol = (Column) json.get("column");

        JSONArray annotationId1 = new JSONArray();
        AnnotationDatas datas1 = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId1.add(index + 1);
        JSONObject jsonStart = new JSONObject();
        jsonStart.put("annotationId", annotationId1);
        jsonStart.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        int flag = 1;
        FocusNode first = focusNodes.get(flag++);
        FocusNode param1 = focusNodes.get(flag++);
        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(first.getValue());
        token1.value = first.getValue();
        token1.type = Constant.AnnotationCategory.FILTER;
        token1.begin = first.getBegin();
        token1.end = first.getEnd();
        datas1.addToken(token1);

        JSONObject arg1 = ColValueOrDateColInst.arg(param1, formulas);
        datas1.addTokens(ColValueOrDateColInst.tokens(param1, formulas, amb));

        FocusNode keyword1 = focusNodes.get(flag++);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(keyword1.getValue());
        token2.value = keyword1.getValue();
        token2.type = Constant.AnnotationCategory.FILTER;
        token2.begin = keyword1.getBegin();
        token2.end = keyword1.getEnd();
        datas1.addToken(token2);

        FocusNode param2 = focusNodes.get(flag++);
        JSONObject arg2 = ColValueOrDateColInst.arg(param2, formulas);
        datas1.addTokens(ColValueOrDateColInst.tokens(param2, formulas, amb));

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
        datas1.addToken(token4);

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

        JSONObject json3 = new JSONObject();
        json3.put("annotationId", annotationId1);
        json3.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json3.put("content", datas1);
        instructions.add(json3);

        return instructions;
    }

}
