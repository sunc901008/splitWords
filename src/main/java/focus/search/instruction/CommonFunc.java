package focus.search.instruction;

import focus.search.base.Common;
import focus.search.meta.Column;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;

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
                    col.setPhysicalName(sourceReceived.physicalName);
                    col.setDbName(sourceReceived.parentDB);
                    columns.add(col);
                }
            }
        }
        return columns;
    }

    // last 返回过滤条件 ["start", "end"]
    public static List<String> params(String key, int param) {
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
        }
        return params;
    }

}
