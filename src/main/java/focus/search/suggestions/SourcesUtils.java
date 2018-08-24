package focus.search.suggestions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Common;
import focus.search.meta.Column;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;

import java.util.*;

/**
 * creator: sunc
 * date: 2018/5/31
 * description:
 */
public class SourcesUtils {
    private static final List<String> randomString = Arrays.asList("hello", "world", "focus", "example");

    // 根据表id获取所有列id
    public static JSONArray getColumnIdList(JSONArray sourceList, List<SourceReceived> srs) {
        JSONArray columnIdList = new JSONArray();
        for (SourceReceived s : srs) {
            if (sourceList.isEmpty()) {
                break;
            }
            if (sourceList.contains(s.tableId)) {
                sourceList.remove(s.tableId);
                s.columns.forEach(c -> columnIdList.add(c.columnId));
            }
        }
        return columnIdList;
    }

    // 根据列名获取列
    public static List<Column> getColumns(String colName, List<SourceReceived> srs) {
        List<Column> columns = new ArrayList<>();
        for (SourceReceived sourceReceived : srs) {
            for (ColumnReceived column : sourceReceived.columns) {
                if (column.columnDisplayName.equalsIgnoreCase(colName)) {
                    Column col = column.transfer();
                    col.setTableId(sourceReceived.tableId);
                    col.setSourceName(sourceReceived.sourceName);
                    col.setTbPhysicalName(sourceReceived.physicalName);
                    col.setDbName(sourceReceived.parentDB);
                    columns.add(col);
                }
            }
        }
        return columns;
    }

    // 获取当前所有列信息
    public static List<Column> colRandomSuggestions(JSONObject user) {
        return colRandomSuggestions(user, new ArrayList<>());
    }

    // 获取指定类型的列信息
    public static List<Column> colRandomSuggestions(JSONObject user, String type) {
        return colRandomSuggestions(user, Collections.singletonList(type));
    }

    // 随机获取几个指定类型的列信息
    public static List<Column> colRandomSuggestions(JSONObject user, List<String> types) {
        List<SourceReceived> srs = JSONArray.parseArray(user.getJSONArray("sources").toJSONString(), SourceReceived.class);
        List<Column> columns = new ArrayList<>();
        for (SourceReceived source : srs) {
            for (ColumnReceived col : source.columns) {
                if (types.isEmpty() || types.contains(col.dataType)) {
                    Column column = col.transfer();
                    column.setSourceName(source.sourceName);
                    column.setTbPhysicalName(source.physicalName);
                    column.setTableId(source.tableId);
                    column.setDbName(source.parentDB);
                    columns.add(column);
                }
            }
        }
        return columns;
    }

    /**
     * 获取数字类型的提示
     *
     * @param isInt 是否为整数
     * @return 数字提示
     */
    public static String decimalSug(boolean isInt) {
        Double d = Math.random() * 100 + 1;
        return isInt ? String.valueOf(d.intValue()) : Common.decimalFormat(d);
    }

    public static String decimalSug() {
        return decimalSug(true);
    }

    public static int decimalSug(int max) {
        Double d = Math.random() * max;
        return d.intValue();
    }

    /**
     * @return 随机的一个字符串单词, 带双引号
     * @see #randomString {@link #randomString}
     */
    public static String stringSug() {
        return String.format("%s%s%s", "\"", randomString.get(decimalSug(randomString.size())), "\"");
    }

    /**
     * @return 随机的一个时间字符串, 带双引号 {@link Common#sdf0}
     */
    public static String dateSug() {
        Date date = Calendar.getInstance().getTime();
        return String.format("%s%s%s", "\"", Common.sdf0.format(date), "\"");
    }

}
