package focus.search.instruction;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.AmbiguitiesRecord;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.AmbiguityDatas;
import focus.search.response.search.IllegalDatas;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
public class CommonFunc {

    public static List<Column> getColumns(String colName, List<SourceReceived> srs) {
        List<Column> columns = new ArrayList<>();
        for (SourceReceived sourceReceived : srs) {
            for (ColumnReceived column : sourceReceived.columns) {
                if (column.columnDisplayName.equalsIgnoreCase(colName)) {
                    Column col = column.transfer();
                    col.setTableId(sourceReceived.tableId);
                    col.setSourceName(sourceReceived.sourceName);
                    col.setTbPhysicalName(sourceReceived.physicalName);
                    col.setDbName(sourceReceived.parentDB);
                    columns.add(col);
                }
            }
        }
        return columns;
    }

    public static JSONObject checkAmb(FocusPhrase focusPhrase, FocusNode fn, List<Column> dateColumns, JSONObject amb, String ambValue) throws IllegalException, AmbiguitiesException {
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
            AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getByValue(ambValue, amb);
            int type = Constant.AmbiguityType.types.indexOf(ambValue) - Constant.AmbiguityType.types.size();
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
                ambiguity = AnnotationToken.getAmbiguityDatas(amb, ambValue, title.toString().trim(), fn.getBegin(), fn.getEnd());
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
        JSONObject json = new JSONObject();
        json.put("column", dateCol);
        json.put("ambiguity", ambiguity);
        return json;
    }

    // last 返回过滤条件 ["start", "end"]
    public static List<String> lastParams(String key, int param) {
        List<String> params = new ArrayList<>();
        Calendar current;
        switch (key) {
            case "day":
                current = Common.getStartDay();
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.DAY_OF_YEAR, 0 - param);
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "week":
                current = Common.getStartWeek();
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.WEEK_OF_YEAR, 0 - param);
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "month":
                current = Common.getStartMonth();
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.MONTH, 0 - param);
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "quarter":
                current = Common.getStartQuarter();
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.MONTH, 0 - param * 3);
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "year":
                current = Common.getStartYear();
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.YEAR, 0 - param);
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "minute":
                current = Common.getStartMinute();
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.MINUTE, 0 - param);
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "hour":
                current = Common.getStartHour();
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.HOUR_OF_DAY, 0 - param);
                params.add(0, Common.biTimeFormat(current));
                return params;
        }
        return params;
    }

    // next 返回过滤条件 ["start", "end"]
    public static List<String> nextParams(String key, int param) {
        List<String> params = new ArrayList<>();
        Calendar current;
        switch (key) {
            case "day":
                current = Common.getStartDay();
                current.add(Calendar.DAY_OF_YEAR, 1);
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.DAY_OF_YEAR, param);
                params.add(Common.biTimeFormat(current));
                return params;
            case "week":
                current = Common.getStartWeek();
                current.add(Calendar.WEEK_OF_YEAR, 1);
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.WEEK_OF_YEAR, param);
                params.add(Common.biTimeFormat(current));
                return params;
            case "month":
                current = Common.getStartMonth();
                current.add(Calendar.MONTH, 1);
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.MONTH, param);
                params.add(Common.biTimeFormat(current));
                return params;
            case "quarter":
                current = Common.getStartQuarter();
                current.add(Calendar.MONTH, 3);
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.MONTH, param * 3);
                params.add(Common.biTimeFormat(current));
                return params;
            case "year":
                current = Common.getStartYear();
                current.add(Calendar.YEAR, 1);
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.YEAR, param);
                params.add(Common.biTimeFormat(current));
                return params;
            case "minute":
                current = Common.getStartMinute();
                current.add(Calendar.MINUTE, 1);
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.MINUTE, param);
                params.add(Common.biTimeFormat(current));
                return params;
            case "hour":
                current = Common.getStartHour();
                current.add(Calendar.HOUR_OF_DAY, param);
                params.add(Common.biTimeFormat(current));
                current.add(Calendar.HOUR_OF_DAY, param);
                params.add(Common.biTimeFormat(current));
                return params;
        }
        return params;
    }

    // <to-date-interval> 返回过滤条件 ["start", "end"]
    public static List<String> params(String key) {
        List<String> params = new ArrayList<>();
        Calendar current;
        Calendar end = Common.getStartDay();
        end.add(Calendar.DAY_OF_YEAR, 1);
        params.add(Common.biTimeFormat(end));
        switch (key) {
            case "today":
                current = Common.getStartDay();
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "week":
                current = Common.getStartWeek();
//                current.add(Calendar.WEEK_OF_YEAR, -1);
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "month":
                current = Common.getStartMonth();
//                current.add(Calendar.MONTH, -1);
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "quarter":
                current = Common.getStartQuarter();
//                current.add(Calendar.MONTH, -3);
                params.add(0, Common.biTimeFormat(current));
                return params;
            case "year":
                current = Common.getStartYear();
//                current.add(Calendar.YEAR, -1);
                params.add(0, Common.biTimeFormat(current));
                return params;
        }
        return params;
    }

    // <ago-interval> 返回过滤条件 "end"
    public static String agoParams(String key, int param) {
        Calendar current = Common.getNow();
        switch (key) {
            case "day":
                current = Common.getStartDay();
                current.add(Calendar.DAY_OF_YEAR, 0 - param);
                return Common.biTimeFormat(current);
            case "week":
                current = Common.getStartWeek();
                current.add(Calendar.WEEK_OF_YEAR, 0 - param);
                return Common.biTimeFormat(current);
            case "month":
                current = Common.getStartMonth();
                current.add(Calendar.MONTH, 0 - param);
                return Common.biTimeFormat(current);
            case "quarter":
                current = Common.getStartQuarter();
                current.add(Calendar.MONTH, 0 - param * 3);
                return Common.biTimeFormat(current);
            case "year":
                current = Common.getStartYear();
                current.add(Calendar.YEAR, 0 - param);
                return Common.biTimeFormat(current);
            case "minute":
                current = Common.getStartMinute();
                current.add(Calendar.MINUTE, 0 - param);
                return Common.biTimeFormat(current);
            case "hour":
                current = Common.getStartHour();
                current.add(Calendar.HOUR_OF_DAY, 0 - param);
                return Common.biTimeFormat(current);
        }
        return Common.biTimeFormat(current);
    }

    // 根据公式名获取公式
    public static Formula getFormula(List<Formula> formulas, String formulaName) throws FocusInstructionException {
        for (Formula f : formulas) {
            if (f.getName().equalsIgnoreCase(formulaName)) {
                return f;
            }
        }
        JSONObject exception = new JSONObject();
        exception.put("formulas", formulas);
        exception.put("formulaName", formulaName);
        throw new FocusInstructionException(exception);
    }

}
