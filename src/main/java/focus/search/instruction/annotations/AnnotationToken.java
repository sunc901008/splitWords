package focus.search.instruction.annotations;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.meta.Column;
import focus.search.response.search.AmbiguityDatas;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/24
 * description:
 */
public class AnnotationToken {
    public String description;
    public String columnName;
    public String tableName;
    public Integer columnId;
    public String type;
    public String detailType;
    public Object value;
    public Integer begin;
    public Integer end;
    public List<String> tokens = new ArrayList<>();
    public AmbiguityDatas ambiguity;

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("description", this.description);
        json.put("columnName", this.columnName);
        json.put("tableName", this.tableName);
        json.put("columnId", this.columnId);
        json.put("type", this.type);
        json.put("detailType", this.detailType);
        json.put("value", this.value);
        json.put("begin", this.begin);
        json.put("columnId", this.columnId);
        json.put("end", this.end);
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(this.tokens);
        if (!jsonArray.isEmpty())
            json.put("tokens", jsonArray);
        return json;
    }

    public static AnnotationToken singleCol(Column column, boolean hasTable, int begin, int end) {
        AnnotationToken token = new AnnotationToken();
        token.description = "column " + column.getColumnDisplayName() + " in " + column.getSourceName();
        token.tableName = column.getSourceName();
        token.columnName = column.getColumnDisplayName();
        token.columnId = column.getColumnId();
        token.type = column.getColumnType().toLowerCase();
        // todo modify detailType
        token.detailType = column.getDataType();
        if (hasTable) {
            token.tokens.add(column.getSourceName());
        }
        token.tokens.add(column.getColumnDisplayName());
        token.value = column.getColumnDisplayName();
        token.begin = begin;
        token.end = end;
        return token;
    }

}
