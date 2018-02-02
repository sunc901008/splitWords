package focus.search.metaReceived;

import focus.search.meta.ColumnModify;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/1
 * description:
 */
public class ColumnModifyReceived {

    public String updated;
    public String name;
    public String source;
    public List<String> samples = new ArrayList<>();

    public ColumnModify transfer() {
        ColumnModify columnModify = new ColumnModify();
        columnModify.setName(this.name);
        columnModify.setUpdated(this.updated);
        return columnModify;
    }

}
