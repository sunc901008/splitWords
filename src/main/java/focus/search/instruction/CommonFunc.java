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
        for (ColumnReceived column : getSources(colName, srs).get(0).columns) {
            if (column.columnDisplayName.equalsIgnoreCase(colName)) {
                return column;
            }
        }
        return null;
    }

    public static ColumnReceived getCol(String colName, SourceReceived srs) {
        for (ColumnReceived column : srs.columns) {
            if (column.columnDisplayName.equalsIgnoreCase(colName)) {
                return column;
            }
        }
        return null;
    }

    public static List<SourceReceived> getSources(String colName, List<SourceReceived> srs) {
        List<SourceReceived> sources = new ArrayList<>();
        for (SourceReceived sr : srs) {
            for (ColumnReceived column : sr.columns) {
                if (column.columnDisplayName.equalsIgnoreCase(colName)) {
                    sources.add(sr);
                    break;
                }
            }
        }
        return sources;
    }

    public static SourceReceived getSource(String sourceName, List<SourceReceived> srs) {
        for (SourceReceived sr : srs) {
            if (sr.sourceName.equals(sourceName))
                return sr;
        }
        return null;
    }

}
