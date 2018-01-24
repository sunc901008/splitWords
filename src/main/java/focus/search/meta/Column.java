package focus.search.meta;

/**
 * creator: sunc
 * date: 2018/1/23
 * description:
 */
public class Column {
    private int id;
    private String name;
    private String tblName;
    private String dataType;
    private String colType;
    private int tblId;

    public Column(int id, String name, String dataType, String colType) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
        this.colType = colType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTblName() {
        return tblName;
    }

    public void setTblName(String tblName) {
        this.tblName = tblName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getColType() {
        return colType;
    }

    public void setColType(String colType) {
        this.colType = colType;
    }

    public int getTblId() {
        return tblId;
    }

    public void setTblId(int tblId) {
        this.tblId = tblId;
    }
}
