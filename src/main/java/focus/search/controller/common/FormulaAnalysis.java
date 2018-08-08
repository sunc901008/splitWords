package focus.search.controller.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.InstructionBuild;
import focus.search.instruction.functionInst.NumberFuncInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.FormulaSettings;

import java.util.*;

import static focus.search.base.Constant.AggregationType.*;

/**
 * creator: sunc
 * date: 2018/3/21
 * description:
 */
public class FormulaAnalysis {

    public static final List<String> func = Arrays.asList("<average-function>", "<count-function>", "<max-function>", "<min-function>",
            "<sum-function>", "<to_double-function>", "<to_integer-function>", "<diff_days-function>", "<month_number-function>",
            "<year-function>", "<strlen-function>", "<number-function>", "<if-then-else-number-function>", "<ifnull-number-function>",
            "<stddev-function>", "<variance-function>", "<unique-count-function>", "<cumulative-function>", "<moving-function>",
            "<group-function>", "<day-function>", "<day-number-of-week-function>", "<day-number-of-year-function>", "<hour-of-day-function>",
            "<diff-time-function>", "<abs-function>", "<acos-function>", "<asin-function>", "<atan-function>", "<cbrt-function>",
            "<ceil-function>", "<sin-function>", "<cos-function>", "<cube-function>", "<exp-function>", "<exp2-function>",
            "<floor-function>", "<ln-function>", "<log10-function>", "<log2-function>", "<sign-function>", "<sq-function>",
            "<sqrt-function>", "<tan-function>", "<greatest-function>", "<least-function>", "<atan2-function>", "<mod-function>",
            "<pow-function>", "<round-function>", "<safe-divide-function>", "<random-function>", "<strpos-function>");

    private static final String BRACKET = "bracket";

    public static final String LEFT_BRACKET = "(";
    private static final String RIGHT_BRACKET = ")";

    // 运算优先级
    private static final List<String> LEVEL0 = Arrays.asList("^");
    private static final List<String> LEVEL1 = Arrays.asList("*", "/");
    private static final List<String> LEVEL2 = Arrays.asList("+", "-");
    //    private static final List<String> LEVEL3 = Arrays.asList(">", "<", "=", "!=");
    private static final List<String> LEVEL4 = Arrays.asList(LEFT_BRACKET, RIGHT_BRACKET);

    // 公式操作符返回的数据类型
    private static final List<String> BOOL_OPERATOR = Arrays.asList(">", "<", "=", "!=");
    private static final List<String> STRING_OPERATOR = Arrays.asList("", "");
    private static final List<String> NUMERIC_OPERATOR = Arrays.asList("*", "/", "+", "-", "^");

    // 聚合类型
    private static final List<String> ALL_AGGREGATION = Arrays.asList(SUM, MIN, MAX, AVERAGE, STD_DEVIATION, VARIANCE, NONE, COUNT, COUNT_DISTINCT);
    // 数据类型
    public static final String TIMESTAMP = "Timestamp";
    public static final String STRING = "String";
    public static final String BOOLEAN = "Boolean";
    public static final String NUMERIC = "Numeric";
    // 列类型
    private static final List<String> MEASURE = Collections.singletonList("MEASURE");
    private static final List<String> ATTRIBUTE = Collections.singletonList("ATTRIBUTE");

    public static class FormulaObj {
        public String type;
        public String name;
        public Object value;
        public JSONArray args = new JSONArray();

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("type", type);
            json.put("name", name);
            json.put("value", value);
            json.put("args", args);
            return json;
        }

        public String toString() {
            return JSON.toJSONString(this);
        }

    }

    public static class Arg {
        public String type;
        public Object value;

        public JSONObject toJSON() {
            return JSONObject.parseObject(JSON.toJSONString(this));
        }
    }

    /**
     * 解析后的表达式转换为后缀表达式
     *
     * @param focusPhrase 解析后的表达式
     * @return 返回后缀表达式
     */
    private static List<Arg> getAfterList(FocusPhrase focusPhrase) {
        List<Arg> args = new ArrayList<>();

        List<FocusNode> focusNodes = focusPhrase.allNode();

        Stack<Arg> stack = new Stack<>();
        for (FocusNode temp : focusNodes) {
            String type = temp.getType();
            if (type.equalsIgnoreCase(Constant.FNDType.TABLE)) {
                continue;
            }
            if (temp.getValue().equals(LEFT_BRACKET)) {// 左括号入栈
                Arg arg = new Arg();
                arg.type = BRACKET;
                arg.value = temp.getValue();
                stack.push(arg);
            } else if (temp.getValue().equals(RIGHT_BRACKET)) {
                while (!stack.peek().value.equals(LEFT_BRACKET)) {// 输出左右括号之前的栈顶元素
                    args.add(stack.pop());
                }
                stack.pop();    // 把左括号弹出
            } else if (type.equalsIgnoreCase(Constant.FNDType.COLUMN)) {     // 若为列
                Arg arg = new Arg();
                arg.type = "column";
                arg.value = temp.getColumn().getColumnId();
                args.add(arg);
            } else if (type.equalsIgnoreCase(Constant.FNDType.INTEGER)) {
                // 若为数字
                Arg arg = new Arg();
                arg.type = "number";
                arg.value = Integer.parseInt(temp.getValue());
                args.add(arg);
            } else if (type.equalsIgnoreCase(Constant.FNDType.DOUBLE)) {
                // 若为数字
                Arg arg = new Arg();
                arg.type = "number";
                arg.value = Double.parseDouble(temp.getValue());
                args.add(arg);
            } else {
                // 从栈中弹出所有优先级比当前运算符高的运算符, 并放进队列中
                while (!stack.isEmpty() && compareOperatorPriority(stack.peek().value.toString(), temp.getValue()) >= 0) {
                    args.add(stack.pop());
                }
                Arg arg = new Arg();
                arg.type = "function";
                arg.value = temp.getValue();
                stack.push(arg);   // 操作符进栈
            }
        }

        // 把栈中的所有元素弹出, 放进队列中
        while (!stack.isEmpty()) {
            args.add(stack.pop());
        }

        return args;
    }

    /**
     * 解析后的表达式转换为后缀表达式
     *
     * @param focusPhrase 解析后的表达式
     * @return 返回后缀表达式
     */
    private static List<Arg> getNumberAfterList(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<Arg> args = new ArrayList<>();

        List<FocusNode> focusNodes = focusPhrase.allFormulaNode();

        Stack<Arg> stack = new Stack<>();
        for (FocusNode temp : focusNodes) {
            if (func.contains(temp.getValue())) {
                Arg arg = new Arg();
                arg.type = Constant.InstType.NUMBER_FUNCTION;
                arg.value = NumberFuncInstruction.arg(temp, null);
                args.add(arg);
                continue;
            }
            String type = temp.getType();
            if (type.equalsIgnoreCase(Constant.FNDType.TABLE)) {
                continue;
            }
            if (temp.getValue().equals(LEFT_BRACKET)) {// 左括号入栈
                Arg arg = new Arg();
                arg.type = "bracket";
                arg.value = temp.getValue();
                stack.push(arg);
            } else if (temp.getValue().equals(RIGHT_BRACKET)) {
                while (!stack.peek().value.equals(LEFT_BRACKET)) {// 输出左右括号之前的栈顶元素
                    args.add(stack.pop());
                }
                stack.pop();    // 把左括号弹出
            } else if (type.equalsIgnoreCase(Constant.FNDType.COLUMN)) {     // 若为普通列
                Arg arg = new Arg();
                arg.type = Constant.InstType.COLUMN;
                arg.value = temp.getColumn().getColumnId();
                args.add(arg);
            } else if (type.equalsIgnoreCase(Constant.FNDType.FORMULA)) {     // 若为公式列
                Arg arg = new Arg();
                for (Formula formula : formulas) {
                    if (temp.getValue().equals(formula.getName())) {
                        arg.value = formula.getInstruction();
                        break;
                    }
                }
                arg.type = Constant.InstType.NUMBER_FUNCTION;
                args.add(arg);
            } else if (type.equalsIgnoreCase(Constant.FNDType.INTEGER)) {
                // 若为数字
                Arg arg = new Arg();
                arg.type = Constant.InstType.NUMBER;
                arg.value = Integer.parseInt(temp.getValue());
                args.add(arg);
            } else if (type.equalsIgnoreCase(Constant.FNDType.DOUBLE)) {
                // 若为数字
                Arg arg = new Arg();
                arg.type = Constant.InstType.NUMBER;
                arg.value = Double.parseDouble(temp.getValue());
                args.add(arg);
            } else {
                // 从栈中弹出所有优先级比当前运算符高的运算符, 并放进队列中
                while (!stack.isEmpty() && compareOperatorPriority(stack.peek().value.toString(), temp.getValue()) >= 0) {
                    args.add(stack.pop());
                }
                Arg arg = new Arg();
                arg.type = Constant.InstType.FUNCTION;
                arg.value = temp.getValue();
                stack.push(arg);   // 操作符进栈
            }
        }

        // 把栈中的所有元素弹出, 放进队列中
        while (!stack.isEmpty()) {
            args.add(stack.pop());
        }

        return args;
    }

    public static FormulaObj analysis(FocusPhrase focusPhrase, JSONObject amb, String language, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        JSONArray instructions = InstructionBuild.build(focusPhrase, 1, amb, language, dateColumns);
        for (int i = 0; i < instructions.size(); i++) {
            JSONObject instruction = instructions.getJSONObject(i);
            if (instruction.getString("instId").equals(Constant.InstIdType.ADD_EXPRESSION)) {
                JSONObject content = instruction.getJSONObject("expression");
                return JSONObject.parseObject(content.toJSONString(), FormulaObj.class);
            }
        }
        throw new FocusInstructionException(focusPhrase.toJSON());
    }

    public static FormulaObj numberAnalysis(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<Arg> args = getNumberAfterList(focusPhrase, formulas);
        Stack<JSONObject> stack = new Stack<>();
        for (Arg arg : args) {
            if (!isOperator(arg.value.toString())) {// 不是操作符
                if (Constant.InstType.NUMBER_FUNCTION.equals(arg.type)) {
                    stack.push((JSONObject) arg.value);
                } else {
                    stack.push(arg.toJSON());
                }
            } else {
                FormulaObj formulaObj = new FormulaObj();
                formulaObj.type = arg.type;
                formulaObj.name = arg.value.toString();
                formulaObj.args.add(stack.pop());
                formulaObj.args.add(0, stack.pop());
                stack.push(formulaObj.toJSON());
            }
        }

        return JSONObject.parseObject(stack.pop().toJSONString(), FormulaObj.class);
    }

    public static FormulaSettings getSettings(FocusPhrase formula) {
        FormulaSettings settings = new FormulaSettings();
        String dataType = STRING;
        String instName = formula.getInstName();
        if ("<filter>".equals(instName)) {// filter
            dataType = BOOLEAN;
        } else {// phrase
            FocusPhrase tmp = formula.getFocusNodes().get(0).getChildren();
            instName = tmp.getInstName();
            if ("<other-function-columns>".equals(instName)) {
                tmp = tmp.getFocusNodes().get(0).getChildren();
                instName = tmp.getInstName();
                List<FocusNode> focusNodes = tmp.getFocusNodes();
                FocusNode fn;
                if ("<if-then-else-function>".equals(instName)) {
                    fn = focusNodes.get(focusNodes.size() - 1);
                } else {
                    fn = focusNodes.get(focusNodes.size() - 2);
                }
                switch (fn.getValue()) {
                    case "<number>":
                    case "<number-source-column>":
                        dataType = NUMERIC;
                        break;
                    case "<all-bool-column>":
                        dataType = BOOLEAN;
                        break;
                    case "<all-date-column>":
                    case "<date-string-value>":
                        dataType = TIMESTAMP;
                        break;
                    case "<single-column-value>":
                    case "<all-string-column>":
                    default:
                        dataType = STRING;
                }
            } else if ("<all-columns>".equals(instName)) {
                tmp = tmp.getFocusNodes().get(0).getChildren();// <XXXXX-columns>
//                tmp = tmp.getFocusNodes().get(0).getChildren();// <XXXXX-function-column>
                instName = tmp.getInstName();// <XXXXX-function-column>
                if ("<number-columns>".equals(instName)) {
                    dataType = NUMERIC;
                } else if ("<string-columns>".equals(instName)) {
                    dataType = STRING;
                } else if ("<bool-columns>".equals(instName)) {
                    dataType = BOOLEAN;
                } else {
                    dataType = TIMESTAMP;
                }
            }
        }
        if (NUMERIC.equals(dataType)) {
            settings.dataType = NUMERIC;
            settings.aggregation = ALL_AGGREGATION;
            settings.columnType = MEASURE;
        } else {
            settings.dataType = dataType;
            settings.aggregation = Collections.singletonList(NONE);
            settings.columnType = ATTRIBUTE;
        }
        return settings;
    }

    private static boolean isOperator(String value) {
        return NUMERIC_OPERATOR.contains(value);
    }

    private static int compareOperatorPriority(String type1, String type2) {
        if (LEVEL0.contains(type1) && LEVEL0.contains(type2)) {
            return 0;
        }
        if (LEVEL1.contains(type1) && LEVEL1.contains(type2)) {
            return 0;
        }
        if (LEVEL2.contains(type1) && LEVEL2.contains(type2)) {
            return 0;
        }
        if (LEVEL4.contains(type1) && LEVEL4.contains(type2)) {
            return 0;
        }
        if (LEVEL0.contains(type1)) {
            return 1;
        }
        if (LEVEL1.contains(type1) && !LEVEL0.contains(type2)) {
            return 1;
        }
        if (LEVEL2.contains(type1) && !LEVEL0.contains(type2) && !LEVEL1.contains(type2)) {
            return 1;
        }
        return -1;
    }

}
