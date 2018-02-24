package focus.search.instruction;

import focus.search.meta.Column;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;

import java.util.ArrayList;
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
                    columns.add(col);
                }
            }
        }
        return columns;
    }

    public static ColumnReceived getCol(String colName, SourceReceived srs) {
        for (ColumnReceived column : srs.columns) {
            if (column.columnDisplayName.equalsIgnoreCase(colName)) {
                return column;
            }
        }
        return null;
    }

    public static SourceReceived getSource(String sourceName, List<SourceReceived> srs) {
        for (SourceReceived sr : srs) {
            if (sr.sourceName.equals(sourceName))
                return sr;
        }
        return null;
    }

}
