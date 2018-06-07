package focus.search.metaReceived;

import focus.search.meta.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/1
 * description:
 */
public class SourceReceived {

    public String parentDB;
    public String sourceName;
    public String physicalName;
    public Integer tableId;
    public String type;
    public List<ColumnReceived> columns = new ArrayList<>();

    public Source transfer() {
        Source source = new Source();
        source.setSourceName(this.sourceName);
        source.setType(this.type);
        source.setTableId(this.tableId);
        source.setDbName(this.parentDB);
        for (ColumnReceived col : this.columns) {
            source.addColumn(col.transfer());
        }
        return source;
    }

}
