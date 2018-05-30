package focus.search.instruction.chineseInstruction.chinesephraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.AmbiguitiesRecord;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.AmbiguityDatas;
import focus.search.response.search.IllegalDatas;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/29
 * description:
 */
//<date-interval> := <daily-interval> |
//        <weekly-interval> |
//        <monthly-interval> |
//        <quarterly-interval> |
//        <yearly-interval>;
public class CDateIntervalInstruction {
    private static final Logger logger = Logger.getLogger(CDateIntervalInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("CDateIntervalInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        String key;
        switch (fn.getValue()) {
            case "<daily-interval>":
                key = "daily";
                break;
            case "<weekly-interval>":
                key = "weekly";
                break;
            case "<monthly-interval>":
                key = "monthly";
                break;
            case "<quarterly-interval>":
                key = "quarterly";
                break;
            case "<yearly-interval>":
                key = "yearly";
                break;
            default:
                key = "daily";
        }
        return build(fn.getChildren(), index, amb, dateColumns, key);
    }

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("CDateIntervalInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFirstNode();

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.DATETIME_INTERVAL);
        json1.put("interval", key);

        Column dateCol;
        AmbiguityDatas ambiguity = null;
        if (dateColumns.size() == 0) {
            // 没有日期列
            String reason = "no date columns in current sources";
            IllegalDatas illegalDatas = new IllegalDatas(fn.getBegin(), fn.getEnd(), reason);
            throw new IllegalException(reason, illegalDatas);
        } else if (dateColumns.size() > 1) {
            // 多个日期列
            // 检测歧义是否解决
            AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getByValue("date_interval", amb);
            int type = Constant.AmbiguityType.types.indexOf("date_interval") - Constant.AmbiguityType.types.size();
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
                ambiguity = AnnotationToken.getAmbiguityDatas(amb, "date_interval", title.toString().trim(), fn.getBegin(), fn.getEnd());
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
                throw new AmbiguitiesException(ars, fn.getBegin(), fn.getEnd(), type);
            }
        } else {
            dateCol = dateColumns.get(0);
        }

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
