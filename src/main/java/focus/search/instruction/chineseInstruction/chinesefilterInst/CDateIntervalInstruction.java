package focus.search.instruction.chineseInstruction.chinesefilterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.filterInst.dateComplexInst.DateIntervalInstruction;
import focus.search.meta.Column;
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
//<date-interval> := <daily-interval> |
//        <weekly-interval> |
//        <monthly-interval> |
//        <quarterly-interval> |
//        <yearly-interval> |
//        <by-day-of-week-interval> |
//        <by-week-interval> |
//        <by-month-interval> |
//        <to-date-interval>;
public class CDateIntervalInstruction {
    private static final Logger logger = Logger.getLogger(CDateIntervalInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("CDateIntervalInstruction instruction arg. focusPhrase:" + focusPhrase.toJSON());
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
            case "<by-day-of-week-interval>":
                key = "day of week";
                break;
            case "<by-week-interval>":
                key = "week";
                break;
            case "<by-month-interval>":
                key = "month";
                break;
            case "<to-date-interval>":
                return buildToDate(fn.getChildren(), index, amb, dateColumns);
            default:
                key = "daily";
        }
        return build(fn.getChildren(), index, amb, dateColumns, key);
    }

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("CDateIntervalInstruction instruction arg. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFirstNode();

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.DATETIME_INTERVAL);
        json1.put("interval", key);

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

        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;
    }

    //    <to-date-interval> := <week-to-date-interval> |
    //                      <month-to-date-interval> |
    //                      <quarter-to-date-interval> |
    //                      <year-to-date-interval>;
    private static JSONArray buildToDate(FocusPhrase focusPhrase, int index, JSONObject amb, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        String key;
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<week-to-date-interval>":
                key = "week";
                break;
            case "<month-to-date-interval>":
                key = "month";
                break;
            case "<quarter-to-date-interval>":
                key = "quarter";
                break;
            case "<year-to-date-interval>":
                key = "year";
                break;
            case "<today-interval>":
                key = "today";
                break;
            default:
                key = "month";
        }
        return DateIntervalInstruction.buildToDate(fn.getChildren(), index, amb, dateColumns, key);
    }

}
