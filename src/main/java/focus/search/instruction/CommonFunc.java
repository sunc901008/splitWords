package focus.search.instruction;

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

    static ColumnReceived getCol(String colName, List<SourceReceived> srs) {
        return getCol0(colName, srs).get(0);
    }


    static List<ColumnReceived> getCol0(String colName, List<SourceReceived> srs) {
        List<ColumnReceived> columns = new ArrayList<>();
        for (SourceReceived sr : srs) {
            for (ColumnReceived column : sr.columns) {
                if (column.columnDisplayName.equalsIgnoreCase(colName)) {
                    columns.add(column);
                }
            }
        }
        return columns;
    }

}
