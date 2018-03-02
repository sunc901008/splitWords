package focus.search.response.exception;

import com.alibaba.fastjson.JSONObject;
import focus.search.meta.AmbiguitiesRecord;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/3/1
 * description:
 */
public class AmbiguitiesException extends Exception {
    public List<AmbiguitiesRecord> ars;
    public Integer position;

    public AmbiguitiesException(List<AmbiguitiesRecord> ars, int position) {
        this.ars = ars;
        this.position = position;
    }

    public String toString() {
        JSONObject json = new JSONObject();
        json.put("position", this.position);
        json.put("ambiguities", this.ars);
        return json.toJSONString();
    }
}
