package focus.search.response.exception;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.meta.AmbiguitiesRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/3/1
 * description:
 */
public class AmbiguitiesException extends Exception {
    public List<AmbiguitiesRecord> ars = new ArrayList<>();
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

    public static AmbiguitiesException recover(String amb) {
        AmbiguitiesException ae = new AmbiguitiesException();
        JSONObject json = JSONObject.parseObject(amb);
        ae.begin = json.getInteger("begin");
        ae.end = json.getInteger("end");
        ae.position = json.getInteger("position");
        JSONArray ars = json.getJSONArray("ars");
        ars.forEach(ar -> ae.ars.add(JSONObject.parseObject(ar.toString(), AmbiguitiesRecord.class)));
        return ae;
    }
}
