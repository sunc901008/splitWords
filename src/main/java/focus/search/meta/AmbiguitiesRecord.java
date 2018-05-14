package focus.search.meta;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/3/1
 * description:
 */
public class AmbiguitiesRecord {
    public int columnId;
    public String type;
    public String columnName;
    public String sourceName;

    public static boolean contains(List<AmbiguitiesRecord> ars, AmbiguitiesRecord a) {
        for (AmbiguitiesRecord ar : ars) {
            if (ar.equals(a)) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(AmbiguitiesRecord a) {
        return columnId == a.columnId && type.equals(a.type) && columnName.equals(a.columnName) && sourceName.equals(a.sourceName);
    }

}
