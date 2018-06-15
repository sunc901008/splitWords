package focus.search.meta;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * creator: sunc
 * date: 2018/3/1
 * description:
 */
public class AmbiguitiesResolve {
    public List<AmbiguitiesRecord> ars = new ArrayList<>();
    public boolean isResolved = false;
    public String value;

    public static AmbiguitiesResolve getByValue(String value, JSONObject amb) {
        if (amb == null) {
            return null;
        }
        for (Object obj : amb.values()) {
            AmbiguitiesResolve tmp = (AmbiguitiesResolve) obj;
            if (Objects.equals(tmp.value, value)) {
                return tmp;
            }
        }
        return null;
    }

    public static AmbiguitiesResolve getById(String id, JSONObject amb) {
        if (!amb.containsKey(id)) {
            return null;
        }
        return JSONObject.parseObject(amb.getJSONObject(id).toJSONString(), AmbiguitiesResolve.class);
    }

    public static String mergeAmbiguities(List<AmbiguitiesRecord> ars, String value, JSONObject amb) {
        String id = UUID.randomUUID().toString();
        for (String key : amb.keySet()) {
            AmbiguitiesResolve tmp = (AmbiguitiesResolve) amb.get(key);
            if (tmp.value.equalsIgnoreCase(value)) {
                tmp.ars = mergeRecord(tmp.ars, ars);
                amb.remove(key);
                amb.put(id, tmp);
                return id;
            }
        }
        AmbiguitiesResolve ar = new AmbiguitiesResolve();
        ar.ars = ars;
        ar.value = value;
        amb.put(id, ar);
        return id;
    }

    private static List<AmbiguitiesRecord> mergeRecord(List<AmbiguitiesRecord> ars1, List<AmbiguitiesRecord> ars2) {
        for (AmbiguitiesRecord ar : ars1) {
            if (!AmbiguitiesRecord.contains(ars2, ar)) {
                ars2.add(ar);
            }
        }
        return ars2;
    }

    public void addRecord(AmbiguitiesRecord record) {
        for (AmbiguitiesRecord ar : ars) {
            if (ar.equals(record)) {
                return;
            }
        }
        ars.add(record);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("isResolved", isResolved);
        json.put("value", value);
        JSONArray j = new JSONArray();
        ars.forEach(ar -> j.add(ar.toJSON()));
        json.put("ars", j);
        return json;
    }

}
