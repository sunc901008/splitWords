package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.ColValueOrDateColInst;
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
 * date: 2018/5/23
 * description:
 */
//<before-after-filter> := <before-filter> |
//        <after-filter>;
public class CBeforeAfterInstruction {
    private static final Logger logger = Logger.getLogger(CBeforeAfterInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws
            FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("CBeforeAfterInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<before-filter>":
                return CBeforeInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<after-filter>":
                return CAfterInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    //<before-chinese> := 之前 |
//        之前的 |
//        以前 |
//        以前的;
//
//<before-filter> := 在 <date-columns> <before-chinese> |
//        <date-columns> <before-chinese> |
//        在 <column-value> <before-chinese> |
//        <column-value> <before-chinese> |
//        <all-date-column> 在 <date-columns> <before-chinese> |
//        <all-date-column> 在 <column-value> <before-chinese>;
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if (Objects.equals("<all-date-column>", first.getValue())) {
            return buildStartsWithCol(focusPhrase, index, amb, formulas, dateColumns, key);
        } else {
            return buildNoCol(focusPhrase, index, amb, formulas, dateColumns, key);
        }
    }

    //<all-date-column> 在 <date-columns> <before-chinese> |
    //<all-date-column> 在 <column-value> <before-chinese>;
    private static JSONArray buildStartsWithCol(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusNode first = focusNodes.get(0);
        Column dateCol;
        FocusNode param;
        FocusPhrase fp = first.getChildren();
        dateCol = fp.getLastNode().getColumn();
        param = focusNodes.get(2);

        datas.addToken(AnnotationToken.singleCol(fp, amb));

        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(focusNodes.get(1).getValue());
        token2.value = focusNodes.get(1).getValue();
        token2.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token2.begin = focusNodes.get(1).getBegin();
        token2.end = focusNodes.get(1).getEnd();
        datas.addToken(token2);

        JSONObject expression = new JSONObject();
        expression.put("name", Constant.SymbolMapper.symbol.get(key));
        expression.put("type", "function");
        JSONArray args = new JSONArray();
        JSONObject arg1 = new JSONObject();
        arg1.put("type", Constant.InstType.COLUMN);
        arg1.put("value", dateCol.getColumnId());
        args.add(arg1);
        args.add(ColValueOrDateColInst.arg(param, formulas));
        expression.put("args", args);

        json1.put("expression", expression);

        instructions.add(json1);

        datas.addTokens(ColValueOrDateColInst.tokens(param, formulas, amb));

        FocusNode last = focusPhrase.getLastNode();
        AnnotationToken token4 = new AnnotationToken();
        token4.addToken(last.getValue());
        token4.value = last.getValue();
        token4.type = Constant.AnnotationTokenType.FILTER;
        token4.begin = last.getBegin();
        token4.end = last.getEnd();
        datas.addToken(token4);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

    //在 <date-columns> <before-chinese> |
    //<date-columns> <before-chinese> |
    //在 <column-value> <before-chinese> |
    //<column-value> <before-chinese> |
    private static JSONArray buildNoCol(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        FocusNode param;
        FocusNode first = focusNodes.get(0);
        if (Objects.equals("在", first.getValue())) {
            param = focusNodes.get(1);
            AnnotationToken token1 = new AnnotationToken();
            token1.addToken(first.getValue());
            token1.value = first.getValue();
            token1.type = Constant.AnnotationTokenType.FILTER;
            token1.begin = first.getBegin();
            token1.end = first.getEnd();
            datas.addToken(token1);
        } else {
            param = first;
        }

        JSONObject json = CommonFunc.checkAmb(focusPhrase, focusPhrase.getFirstNode(), dateColumns, amb, key);
        AmbiguityDatas ambiguity = (AmbiguityDatas) json.get("ambiguity");
        Column dateCol = (Column) json.get("column");

        datas.addTokens(ColValueOrDateColInst.tokens(param, formulas, amb));

        JSONObject expression = new JSONObject();
        expression.put("name", Constant.SymbolMapper.symbol.get(key));
        expression.put("type", "function");
        JSONArray args = new JSONArray();
        JSONObject arg1 = new JSONObject();
        arg1.put("type", Constant.InstType.COLUMN);
        arg1.put("value", dateCol.getColumnId());
        args.add(arg1);
        args.add(ColValueOrDateColInst.arg(param, formulas));
        expression.put("args", args);

        json1.put("expression", expression);

        instructions.add(json1);

        FocusNode last = focusPhrase.getLastNode();
        AnnotationToken token3 = new AnnotationToken();
        token3.description = "column " + dateCol.getColumnDisplayName() + " in " + dateCol.getSourceName();
        token3.tableName = dateCol.getSourceName();
        token3.columnName = dateCol.getColumnDisplayName();
        token3.columnId = dateCol.getColumnId();
        token3.addToken(last.getValue());
        token3.value = last.getValue();
        token3.type = Constant.AnnotationTokenType.FILTER;
        token3.begin = last.getBegin();
        token3.end = last.getEnd();
        token3.ambiguity = ambiguity;
        datas.addToken(token3);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

}
