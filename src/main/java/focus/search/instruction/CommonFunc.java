package focus.search.instruction;

import com.sun.istack.internal.NotNull;
import focus.search.meta.Column;
import focus.search.meta.Formula;
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

    @NotNull
    public static Formula getFormula(List<Formula> formulas, String formulaName) {
        for (Formula f : formulas) {
            if (f.getName().equals(formulaName)) {
                return f;
            }
        }
        return null;
    }

}
