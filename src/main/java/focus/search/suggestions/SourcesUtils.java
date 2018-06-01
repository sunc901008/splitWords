package focus.search.suggestions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Common;
import focus.search.meta.Column;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/31
 * description:
 */
public class SourcesUtils {

    // 获取当前所有列信息
    public static List<Column> colRandomSuggestions(JSONObject user) {
        return colRandomSuggestions(user, "");
    }

    // 获取指定类型的列信息
    public static List<Column> colRandomSuggestions(JSONObject user, String type) {
        List<SourceReceived> srs = JSONArray.parseArray(user.getJSONArray("sources").toJSONString(), SourceReceived.class);
        List<Column> columns = new ArrayList<>();
        for (SourceReceived source : srs) {
            for (ColumnReceived col : source.columns) {
                if (Common.isEmpty(type) || col.dataType.equals(type)) {
                    Column column = col.transfer();
                    column.setSourceName(source.sourceName);
                    column.setPhysicalName(source.physicalName);
                    column.setTableId(source.tableId);
                    columns.add(column);
                }
            }
        }
        return columns;
    }
}
