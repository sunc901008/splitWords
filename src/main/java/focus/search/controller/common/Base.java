package focus.search.controller.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.FocusParser;
import focus.search.instruction.CommonFunc;
import focus.search.meta.AmbiguitiesRecord;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.metaReceived.SourceReceived;

import java.util.List;
import java.util.UUID;

/**
 * creator: sunc
 * date: 2018/5/7
 * description: control 中的一些公共方法
 */
public class Base {
    public static final FocusParser englishParser;
    public static final FocusParser chineseParser;

    static {
        englishParser = new FocusParser(Constant.Language.ENGLISH);
        chineseParser = new FocusParser(Constant.Language.CHINESE);
    }

    /**
     * 根据context恢复歧义/语言 等
     *
     * @param contextJson json 类型的context
     * @param srs         source tables
     * @return json 格式的歧义
     */
    public static JSONObject context(JSONObject contextJson, List<SourceReceived> srs) {
        JSONObject ambiguities = new JSONObject();
        String contextStr = contextJson.getString("disambiguations");// 检测 lisp 返回的空值 nil
        if (!Common.isEmpty(contextStr) && !contextStr.equalsIgnoreCase("NIL")) {

            // 恢复歧义
            JSONArray disambiguations = JSONArray.parseArray(contextStr);

            for (Object obj : disambiguations) {
                JSONObject col = JSONObject.parseObject(obj.toString());
                String columnName = col.getString("columnName");
                int columnId = col.getInteger("columnId");
                AmbiguitiesResolve ar = new AmbiguitiesResolve();
                ar.value = columnName;
                ar.isResolved = true;

                List<Column> columns = CommonFunc.getColumns(columnName, srs);
                for (Column column : columns) {
                    AmbiguitiesRecord ambiguitiesRecord = new AmbiguitiesRecord();
                    ambiguitiesRecord.sourceName = column.getSourceName();
                    ambiguitiesRecord.columnName = columnName;
                    ambiguitiesRecord.columnId = column.getColumnId();
                    ambiguitiesRecord.type = Constant.FNDType.COLUMN;
                    ar.ars.add(ambiguitiesRecord);
                }
                for (AmbiguitiesRecord a : ar.ars) {
                    if (a.columnId == columnId) {
                        ar.ars.remove(a);
                        ar.ars.add(0, a);
                        break;
                    }
                }

                ambiguities.put(UUID.randomUUID().toString(), ar);
            }
        }
        return ambiguities;
    }

    /**
     * 检测 answer 依赖列的修改是否影响 answer
     *
     * @param columns 所有修改的列
     * @param col     answer 依赖的列
     * @return bool
     */
    public static boolean affect(JSONArray columns, Column col) {
        for (int i = 0; i < columns.size(); i++) {
            JSONObject column = columns.getJSONObject(i);
            if (col.getColumnId() == column.getInteger("id")) {
                return !col.getColumnDisplayName().equalsIgnoreCase(column.getString("columnDisplayName"));
            }
        }
        return false;
    }

}
