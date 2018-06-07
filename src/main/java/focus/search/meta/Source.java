package focus.search.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/1
 * description:
 */
public class Source {

    private String dbName;
    private String sourceName;
    private Integer tableId;
    private String type;
    private List<Column> columns = new ArrayList<>();

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

}
