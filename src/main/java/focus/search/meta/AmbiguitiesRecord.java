package focus.search.meta;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Objects;

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
    // 用户实际输入
    public String realValue;
    // 可能产生的歧义
    public String possibleValue;

    public AmbiguitiesRecord() {

    }

    public AmbiguitiesRecord(String type, String realValue) {
        this.type = type;
        this.realValue = realValue;
    }


    public static boolean contains(List<AmbiguitiesRecord> ars, AmbiguitiesRecord a) {
        for (AmbiguitiesRecord ar : ars) {
            if (ar.equals(a)) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(AmbiguitiesRecord a) {
        return columnId == a.columnId && Objects.equals(type, a.type)
                && Objects.equals(columnName, a.columnName) && Objects.equals(sourceName, a.sourceName)
                && Objects.equals(realValue, a.realValue) && Objects.equals(possibleValue, a.possibleValue);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("columnId", columnId);
        json.put("type", type);
        json.put("columnName", columnName);
        json.put("sourceName", sourceName);
        json.put("realValue", realValue);
        json.put("possibleValue", possibleValue);
        return json;
    }

}
