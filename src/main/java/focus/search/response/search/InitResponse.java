package focus.search.response.search;

import focus.search.meta.Formula;
import focus.search.meta.Source;
import focus.search.response.JSONFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/1
 * description:
 */
public class InitResponse implements JSONFormat {
    private String status;
    private String message;
    private List<Source> sources = new ArrayList<>();
    private List<Formula> formulas;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public void addSource(Source source) {
        this.sources.add(source);
    }

}
