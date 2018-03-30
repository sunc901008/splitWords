package focus.search.controller.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/3/26
 * description:
 */
public class FormulaCase {

// average、count、max、min、sum、to_date、to_string、to_double、to_integer、
// diff_days、month、month_number、year、！=、<、>、<=、>=、=、+、-、*、/、
// and、if...then...else、ifnull、isnull、not、or、concat、contains、strlen、substr

    private static final List<String> TYPE1 = Arrays.asList("+", "-", ">", "<", ">=", "<=", "=", "!=", "*", "/");
    private static final List<String> DATA_TYPE1 = Arrays.asList("double", "int", "bigint", "smallint");

    public static JSONArray buildCase(JSONObject user, String keyword) {
        if (TYPE1.contains(keyword)) {
            return buildCase1(user, keyword);
        }
        return null;
    }

    /**
     * 简单的数字类型操作
     *
     * @param user    session user info
     * @param keyword keyword
     * @return jsonArray of case
     * @see #TYPE1 {@link #DATA_TYPE1}
     */
    private static JSONArray buildCase1(JSONObject user, String keyword) {
        JSONArray cases = new JSONArray();
        List<SourceReceived> srs = JSONArray.parseArray(user.getJSONArray("sources").toJSONString(), SourceReceived.class);
        cases.add(SuggestionBuild.decimalSug() + keyword + SuggestionBuild.decimalSug());
        cases.add(SuggestionBuild.decimalSug(false) + keyword + SuggestionBuild.decimalSug(false));
        List<String> columns = new ArrayList<>();
        for (SourceReceived source : srs) {
            if (columns.size() >= 2) {
                break;
            }
            for (ColumnReceived column : source.columns) {
                if (columns.size() >= 2) {
                    break;
                }
                if (DATA_TYPE1.contains(column.dataType.toLowerCase()) && !columns.contains(column.columnDisplayName)) {
                    columns.add(column.columnDisplayName);
                }
            }
        }
        if (columns.size() == 1) {
            cases.add(columns.get(0) + keyword + SuggestionBuild.decimalSug());
        } else if (columns.size() == 2) {
            cases.add(columns.get(0) + keyword + columns.get(1));
        }
        return cases;
    }

}
