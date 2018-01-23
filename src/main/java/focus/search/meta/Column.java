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

    public Column(int id, String name, String tblName) {
        this.id = id;
        this.name = name;
        this.tblName = tblName;
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
}
