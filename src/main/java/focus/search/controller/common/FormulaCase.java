package focus.search.controller.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.instruction.functionInst.DateFunc.ToDateFuncInstruction;
import focus.search.instruction.functionInst.StringFunc.ConcatFuncInstruction;
import focus.search.instruction.functionInst.StringFunc.MonthFuncInstruction;
import focus.search.instruction.functionInst.StringFunc.SubstrFuncInstruction;
import focus.search.instruction.functionInst.StringFunc.ToStringFuncInstruction;
import focus.search.instruction.functionInst.boolFunc.ContainsFuncInstruction;
import focus.search.instruction.functionInst.boolFunc.IsNullFuncInstruction;
import focus.search.instruction.functionInst.boolFunc.NotFuncInstruction;
import focus.search.instruction.functionInst.boolFunc.ToBoolFuncInstruction;
import focus.search.instruction.functionInst.numberFunc.*;
import focus.search.meta.Column;

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

    private static final List<String> MATH_TYPE = Arrays.asList("+", "-", "*", "/");
    private static final List<String> BOOL_TYPE = Arrays.asList(">", "<", ">=", "<=", "=", "!=");
    public static final List<String> boolFunc = Arrays.asList("isnull ( %s )", "to_bool ( %s )");

    /**
     * @param user    websocket用户信息
     * @param keyword keyword
     * @return formula case
     */
    public static JSONArray buildCase(JSONObject user, String keyword) {
        return buildCase(user, keyword, 2);
    }

    /**
     * @param user    websocket用户信息
     * @param keyword keyword
     * @param count   formula case 个数
     * @return formula case
     */
    public static JSONArray buildCase(JSONObject user, String keyword, int count) {
        JSONArray jsonArray;
        if (MATH_TYPE.contains(keyword) || BOOL_TYPE.contains(keyword)) {
            jsonArray = buildCaseSimple(user, keyword);
        } else {
            jsonArray = buildCaseOther(user, keyword);
        }
        JSONArray cases = new JSONArray();
        if (jsonArray == null || jsonArray.size() <= count) {
            cases = jsonArray;
        } else {
            while (cases.size() < count) {
                String value = jsonArray.get(SuggestionBuild.decimalSug(jsonArray.size())).toString();
                if (!cases.contains(value)) {
                    cases.add(value);
                }
            }
        }
        return cases;
    }

    /**
     * 简单的数字类型操作
     *
     * @param user    session user info
     * @param keyword keyword
     * @return jsonArray of case
     * @see #MATH_TYPE {@link #MATH_TYPE} #BOOL_TYPE {@link #BOOL_TYPE}
     */
    private static JSONArray buildCaseSimple(JSONObject user, String keyword) {
        JSONArray cases = new JSONArray();
        cases.add(SuggestionBuild.decimalSug() + keyword + SuggestionBuild.decimalSug());
        cases.add(SuggestionBuild.decimalSug(false) + keyword + SuggestionBuild.decimalSug(false));
        List<Column> numberColumns = SuggestionBuild.colRandomSuggestions(user, Arrays.asList(Constant.DataType.INT, Constant.DataType.DOUBLE));
        String value1 = numberColumns.get(SuggestionBuild.decimalSug(numberColumns.size())).getColumnDisplayName();
        String value2 = numberColumns.get(SuggestionBuild.decimalSug(numberColumns.size())).getColumnDisplayName();
        cases.add(value1 + keyword + SuggestionBuild.decimalSug());
        cases.add(value1 + keyword + value2);
        return cases;
    }

    /**
     * 其他函数
     *
     * @param user    session user info
     * @param keyword keyword
     * @return jsonArray of case
     */
    private static JSONArray buildCaseOther(JSONObject user, String keyword) {
        switch (keyword) {
            case "average":
                return AverageFuncInstruction.buildCase(user);
            case "count":
                return CountFuncInstruction.buildCase(user);
            case "max":
            case "min":
                return MaxMinFuncInstruction.buildCase(user, keyword);
            case "sum":
                return SumFuncInstruction.buildCase(user);
            case "to_date":
                return ToDateFuncInstruction.buildCase(user);
            case "to_string":
                return ToStringFuncInstruction.buildCase(user);
            case "to_double":
            case "to_integer":
                return ToIntegerDoubleFuncInstruction.buildCase(user, keyword);
            case "to_bool":
                return ToBoolFuncInstruction.buildCase(user);
            case "diff_days":
                return DiffDaysFuncInstruction.buildCase(user);
            case "month":
                return MonthFuncInstruction.buildCase(user);
            case "month_number":
            case "year":
                return MonthNumberYearFuncInstruction.buildCase(user, keyword);
            case "and":
            case "or":
                return buildCaseAndOr(user, keyword);
            case "if":
                return buildCaseIf(user);
            case "ifnull":
                return buildCaseIfNull(user);
            case "isnull":
                return IsNullFuncInstruction.buildCase(user);
            case "not":
                return NotFuncInstruction.buildCase(user);
            case "concat":
                return ConcatFuncInstruction.buildCase(user);
            case "contains":
                return ContainsFuncInstruction.buildCase(user);
            case "strlen":
                return StrlenFuncInstruction.buildCase(user);
            case "substr":
                return SubstrFuncInstruction.buildCase(user);
            default:
                return null;
        }
    }

    // formula case:number
    public static JSONArray buildCaseNumber(String example) {
        JSONArray cases = new JSONArray();
        cases.add(String.format(example, SuggestionBuild.decimalSug()));
        cases.add(String.format(example, SuggestionBuild.decimalSug(false)));
        return cases;
    }

    // formula case:number column
    public static JSONArray buildCaseNumberCol(String example, JSONObject user) {
        JSONArray cases = new JSONArray();
        cases.addAll(buildCaseAllCol(example, user, Constant.DataType.INT));
        cases.addAll(buildCaseAllCol(example, user, Constant.DataType.DOUBLE));
        return cases;
    }

    // formula case:date column
    public static JSONArray buildCaseDateCol(String example, JSONObject user) {
        return buildCaseAllCol(example, user, Constant.DataType.TIMESTAMP);
    }

    // formula case:string column
    public static JSONArray buildCaseStringCol(String example, JSONObject user) {
        return buildCaseAllCol(example, user, Constant.DataType.STRING);
    }

    // formula case:bool column
    public static JSONArray buildCaseBoolCol(String example, JSONObject user) {
        return buildCaseAllCol(example, user, Constant.DataType.BOOLEAN);
    }

    // formula case:all column
    private static JSONArray buildCaseAllCol(String example, JSONObject user, String type) {
        JSONArray cases = new JSONArray();
        List<Column> columns = SuggestionBuild.colRandomSuggestions(user, type);
        if (columns.size() > 0) {
            cases.add(String.format(example, columns.get(SuggestionBuild.decimalSug(columns.size())).getColumnDisplayName()));
        }
        return cases;
    }

    // formula case:all column
    public static JSONArray buildCaseAllCol(String example, JSONObject user) {
        return buildCaseAllCol(example, user, null);
    }

    // formula case: and or
    private static JSONArray buildCaseAndOr(JSONObject user, String keyword) {
        String example = "%s %s %s";
        JSONArray cases = new JSONArray();
        List<Column> columns = SuggestionBuild.colRandomSuggestions(user, Constant.DataType.INT);
        if (columns.size() > 0) {
            String name = columns.get(SuggestionBuild.decimalSug(columns.size())).getColumnDisplayName();
            String value1 = String.format(boolFunc.get(SuggestionBuild.decimalSug(boolFunc.size())), name);
            String value2 = String.format(boolFunc.get(SuggestionBuild.decimalSug(boolFunc.size())), name);
            cases.add(String.format(example, value1, keyword, value2));
        }
        String value1 = SuggestionBuild.decimalSug() + BOOL_TYPE.get(SuggestionBuild.decimalSug(BOOL_TYPE.size())) + SuggestionBuild.decimalSug();
        String value2 = SuggestionBuild.decimalSug() + BOOL_TYPE.get(SuggestionBuild.decimalSug(BOOL_TYPE.size())) + SuggestionBuild.decimalSug();
        cases.add(String.format(example, value1, keyword, value2));
        return cases;
    }

    // formula case: if then else
    private static JSONArray buildCaseIf(JSONObject user) {
        String example = "if %s then %s else %s";
        JSONArray cases = new JSONArray();
        List<Column> intColumns = SuggestionBuild.colRandomSuggestions(user, Constant.DataType.INT);
        if (intColumns.size() > 1) {
            String name = intColumns.get(SuggestionBuild.decimalSug(intColumns.size())).getColumnDisplayName();
            String value1 = String.format(boolFunc.get(SuggestionBuild.decimalSug(boolFunc.size())), name);
            String value2 = intColumns.get(0).getColumnDisplayName();
            String value3 = intColumns.get(1).getColumnDisplayName();
            cases.add(String.format(example, value1, value2, value3));
        }
        if (intColumns.size() > 0) {
            String name = intColumns.get(SuggestionBuild.decimalSug(intColumns.size())).getColumnDisplayName();
            String value1 = String.format(boolFunc.get(SuggestionBuild.decimalSug(boolFunc.size())), name);
            String value2 = SuggestionBuild.decimalSug();
            String value3 = SuggestionBuild.decimalSug();
            cases.add(String.format(example, value1, value2, value3));
        }
        String value1 = SuggestionBuild.decimalSug() + BOOL_TYPE.get(SuggestionBuild.decimalSug(BOOL_TYPE.size())) + SuggestionBuild.decimalSug();
        List<Column> stringColumns = SuggestionBuild.colRandomSuggestions(user, Constant.DataType.STRING);
        if (stringColumns.size() == 1) {
            String value2 = stringColumns.get(0).getColumnDisplayName();
            cases.add(String.format(example, value1, value2, value2));
        } else if (stringColumns.size() > 1) {
            String value2 = stringColumns.get(0).getColumnDisplayName();
            String value3 = stringColumns.get(1).getColumnDisplayName();
            cases.add(String.format(example, value1, value2, value3));
        }
        return cases;
    }

    // formula case: ifnull
    private static JSONArray buildCaseIfNull(JSONObject user) {
        String example = "ifnull(%s , %s)";
        JSONArray cases = new JSONArray();
        List<Column> intColumns = SuggestionBuild.colRandomSuggestions(user, Constant.DataType.INT);
        if (intColumns.size() > 1) {
            String value1 = intColumns.get(0).getColumnDisplayName();
            String value2 = intColumns.get(1).getColumnDisplayName();
            cases.add(String.format(example, value1, value2));
        }
        if (intColumns.size() > 0) {
            String value1 = SuggestionBuild.decimalSug();
            String value2 = SuggestionBuild.decimalSug();
            cases.add(String.format(example, value1, value2));
        }
        List<Column> stringColumns = SuggestionBuild.colRandomSuggestions(user, Constant.DataType.STRING);
        if (stringColumns.size() == 1) {
            String value1 = stringColumns.get(0).getColumnDisplayName();
            cases.add(String.format(example, value1, value1));
        } else if (stringColumns.size() > 1) {
            String value1 = stringColumns.get(0).getColumnDisplayName();
            String value2 = stringColumns.get(1).getColumnDisplayName();
            cases.add(String.format(example, value1, value2));
        }
        return cases;
    }

}
