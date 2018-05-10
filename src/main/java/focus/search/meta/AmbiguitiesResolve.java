package focus.search.meta;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Common;

import java.util.ArrayList;
import java.util.List;

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
        for (Object obj : amb.values()) {
            AmbiguitiesResolve tmp = (AmbiguitiesResolve) obj;
            if (tmp.value.equalsIgnoreCase(value)) {
                return tmp;
            }
        }
        return null;
    }

    public static AmbiguitiesResolve getById(String id, JSONObject amb) {
        String ambiguities = amb.getString(id);
        if (Common.isEmpty(ambiguities)) {
            return null;
        }
        return JSONObject.parseObject(ambiguities, AmbiguitiesResolve.class);
    }

}
