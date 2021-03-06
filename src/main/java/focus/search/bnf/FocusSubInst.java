package focus.search.bnf;

import java.util.ArrayList;
import java.util.List;

/**
 * user: sunc
 * data: 2018/1/26.
 */
public class FocusSubInst {
    private int index;
    private boolean error = false;
    private List<FocusPhrase> fps = new ArrayList<>();

    public FocusSubInst() {

    }

    public FocusSubInst(int index, List<FocusPhrase> fps) {
        this.index = index;
        this.fps = fps;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<FocusPhrase> getFps() {
        return fps;
    }

    public boolean isSuggestion() {
        return fps.get(0).isSuggestion();
    }

    public void setFps(List<FocusPhrase> fps) {
        this.fps = fps;
    }

    public void addFps(FocusPhrase fp) {
        this.fps.add(fp);
    }

    public void addAllFps(List<FocusPhrase> fps) {
        this.fps.addAll(fps);
    }

    public boolean isError() {
        return error;
    }

    public void setError() {
        this.error = true;
    }

    public boolean isEmpty() {
        return fps.isEmpty();
    }

}
