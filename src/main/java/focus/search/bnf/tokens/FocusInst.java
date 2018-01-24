package focus.search.bnf.tokens;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusInst {

    private List<FocusPhraseOrFilter> pfs = new ArrayList<>();

    public List<FocusPhraseOrFilter> getPfs() {
        return pfs;
    }

    public void setPfs(List<FocusPhraseOrFilter> pfs) {
        this.pfs = pfs;
    }

    public void addPfs(FocusPhraseOrFilter pf) {
        this.pfs.add(pf);
    }

}
