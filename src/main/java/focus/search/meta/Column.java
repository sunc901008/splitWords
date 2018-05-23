package focus.search.meta;

/**
 * creator: sunc
 * date: 2018/1/23
 * description:
 */
public class Column {
    private int columnId;
    private String columnName;
    private String columnDisplayName;
    private String dataType;
    private String columnType;
    private String description;
    private ColumnModify columnModify;

    private String sourceName;
    private String physicalName;
    private Integer tableId;
    private String aggregation;

    public Column() {
    }

    public Column(int columnId, String columnDisplayName, String dataType, String columnType) {
        this.columnId = columnId;
        this.columnDisplayName = columnDisplayName;
        this.dataType = dataType;
        this.columnType = columnType;
    }

    public int getColumnId() {
        return columnId;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnDisplayName() {
        return columnDisplayName;
    }

    public void setColumnDisplayName(String columnDisplayName) {
        this.columnDisplayName = columnDisplayName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ColumnModify getColumnModify() {
        return columnModify;
    }

    public void setColumnModify(ColumnModify columnModify) {
        this.columnModify = columnModify;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getPhysicalName() {
        return physicalName;
    }

    public void setPhysicalName(String physicalName) {
        this.physicalName = physicalName;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public String getAggregation() {
        return aggregation;
    }

    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }
}
