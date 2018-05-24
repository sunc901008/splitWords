package focus.search.instruction.filterInst.dateComplexInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
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
 * date: 2018/5/23
 * description:
 */
//<before-after-filter> := <before-filter> |
//        <after-filter>;
public class BeforeAfterInstruction {
    private static final Logger logger = Logger.getLogger(BeforeAfterInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws
            FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("BeforeAfterInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<before-filter>":
                return BeforeInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            case "<after-filter>":
                return AfterInstruction.build(fn.getChildren(), index, amb, formulas, dateColumns);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
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
        AmbiguityDatas ambiguity = null;
        if (Objects.equals("<all-date-column>", first.getValue())) {
            FocusPhrase fp = first.getChildren();
            dateCol = fp.getLastNode().getColumn();
            param = focusNodes.get(2);

            datas.addToken(AnnotationToken.singleCol(fp, amb));

            AnnotationToken token2 = new AnnotationToken();
            token2.addToken(key);
            token2.value = key;
            token2.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
            token2.begin = focusNodes.get(1).getBegin();
            token2.end = focusNodes.get(1).getEnd();
            datas.addToken(token2);
        } else {
            // TODO: 2018/5/24 当前数据源中查找日期列
            if (dateColumns.size() == 0) {
                // 没有日期列
                String reason = "no date columns in current sources";
                IllegalDatas illegalDatas = new IllegalDatas(focusPhrase.getFirstNode().getBegin(), focusPhrase.getLastNode().getEnd(), reason);
                throw new IllegalException(reason, illegalDatas);
            } else if (dateColumns.size() > 1) {
                // 多个日期列
                // 检测歧义是否解决
                AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getByValue(key, amb);
                int type = "before".equals(key) ? Constant.AmbiguityType.BEFORE : Constant.AmbiguityType.AFTER;
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
                    ambiguity = AnnotationToken.getAmbiguityDatas(amb, key, title.toString().trim(), focusPhrase.getFirstNode().getBegin(), focusPhrase.getLastNode().getEnd());
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

            param = focusNodes.get(1);

            AnnotationToken token2 = new AnnotationToken();
            token2.description = "column " + dateCol.getColumnDisplayName() + " in " + dateCol.getSourceName();
            token2.tableName = dateCol.getSourceName();
            token2.columnName = dateCol.getColumnDisplayName();
            token2.columnId = dateCol.getColumnId();
            token2.addToken(key);
            token2.value = key;
            token2.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
            token2.begin = first.getBegin();
            token2.end = first.getEnd();
            token2.ambiguity = ambiguity;
            datas.addToken(token2);
        }

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

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }


}
