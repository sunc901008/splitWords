package focus.search.meta;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/3/1
 * description:
 */
public class AmbiguitiesResolve {
    public List<AmbiguitiesRecord> ars;
    public boolean isResolved = false;
    public String value;

    public AmbiguitiesResolve(List<AmbiguitiesRecord> ars) {
        this.ars = ars;
    }

}
