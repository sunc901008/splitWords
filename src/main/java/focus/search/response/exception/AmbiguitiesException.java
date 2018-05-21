package focus.search.response.exception;

import com.alibaba.fastjson.JSONArray;
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
    public int begin;
    public int end;
    public int position;

    public AmbiguitiesException() {

    }

    public AmbiguitiesException(List<AmbiguitiesRecord> ars, int begin, int end, int position) {
        this.ars = ars;
        this.begin = begin;
        this.end = end;
        this.position = position;
    }

    public AmbiguitiesException(List<AmbiguitiesRecord> ars, int begin, int end) {
        this.ars = ars;
        this.begin = begin;
        this.end = end;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("begin", this.begin);
        json.put("end", this.end);
        json.put("position", this.position);
//        JSONArray ars = new JSONArray();
//        this.ars.forEach(ar -> ars.add(ar.toJSON()));
        json.put("ars", ars);
        return json;
    }

    public String toString() {
        return toJSON().toJSONString();
    }
}
