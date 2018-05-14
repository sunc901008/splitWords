package focus.search.metaReceived;

import focus.search.meta.Column;

/**
 * creator: sunc
 * date: 2018/1/23
 * description:
 */
public class ColumnReceived {
    public int columnId;
    public String columnName;
    public String columnDisplayName;
    public String dataType;
    public String columnType;
    public String description;
    public ColumnModifyReceived columnModify;
    public String numFormat;
    public String dateFormat;
    public int hidden;
    public String synonyms;
    public String aggregation;
    public int attributeDimension;
    public int priority;
    public String currencyFormat;
    public String indexType;
    public String geoType;
    public String physicalName;
    public String DBName;
    public int additive;

    public Column transfer() {
        Column column = new Column();
        column.setColumnDisplayName(this.columnDisplayName);
        column.setColumnId(this.columnId);
        column.setColumnName(this.columnName);
        column.setDataType(this.dataType);
        column.setDescription(this.description);
        column.setColumnType(this.columnType);
        column.setColumnModify(this.columnModify.transfer());
        column.setAggregation(this.aggregation);
        return column;
    }

}