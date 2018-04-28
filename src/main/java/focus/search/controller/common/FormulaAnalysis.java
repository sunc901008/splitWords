package focus.search.controller.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.InstructionBuild;
import focus.search.response.search.FormulaSettings;

import java.util.*;

/**
 * creator: sunc
 * date: 2018/3/21
 * description:
 */
public class FormulaAnalysis {
    private static final String LEFT_BRACKET = "(";
    private static final String RIGHT_BRACKET = ")";

    // 运算优先级
    private static final List<String> LEVEL1 = Arrays.asList("*", "/");
    private static final List<String> LEVEL2 = Arrays.asList("+", "-");
    //    private static final List<String> LEVEL3 = Arrays.asList(">", "<", "=", "!=");
    private static final List<String> LEVEL4 = Arrays.asList(LEFT_BRACKET, RIGHT_BRACKET);

    // 公式操作符返回的数据类型
    private static final List<String> BOOL_OPERATOR = Arrays.asList(">", "<", "=", "!=");
    private static final List<String> STRING_OPERATOR = Arrays.asList("", "");
    private static final List<String> NUMERIC_OPERATOR = Arrays.asList("*", "/", "+", "-", "^");

    // 聚合类型
    private static final List<String> ALL_AGGREGATION = Arrays.asList("SUM", "MIN", "MAX", "AVERAGE", "STD_DEVIATION", "VARIANCE", "NONE", "COUNT",
            "COUNT_DISTINCT");
    private static final List<String> NONE = Collections.singletonList("NONE");
    // 数据类型
    private static final String TIMESTAMP = "Timestamp";
    private static final String STRING = "String";
    private static final String BOOLEAN = "Boolean";
    private static final String NUMERIC = "Numeric";
    // 列类型
    private static final List<String> MEASURE = Collections.singletonList("MEASURE");
    private static final List<String> ATTRIBUTE = Collections.singletonList("ATTRIBUTE");

    public static class FormulaObj {
        public String type;
        public String name;
        public JSONArray args = new JSONArray();

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("type", type);
            json.put("name", name);
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
                arg.type = "bracket";
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

    public static FormulaObj analysis(FocusPhrase focusPhrase) throws InvalidRuleException {
        JSONArray instructions = InstructionBuild.build(focusPhrase, 1, new JSONObject(), new ArrayList<>());
        for (int i = 0; i < instructions.size(); i++) {
            JSONObject instruction = instructions.getJSONObject(i);
            if (instruction.getString("instId").equals("add_expression")) {
                JSONObject content = instruction.getJSONObject("expression");
                return JSONObject.parseObject(content.toJSONString(), FormulaObj.class);
            }
        }
        throw new InvalidRuleException("Build instruction fail!!!");
    }

    public static FormulaObj numberAnalysis(FocusPhrase focusPhrase) {
        List<Arg> args = getAfterList(focusPhrase);
        Stack<JSONObject> stack = new Stack<>();
        for (Arg arg : args) {
            if (!isOperator(arg.value.toString())) {// 不是操作符
                stack.push(arg.toJSON());
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

    public static FormulaSettings getSettings(FormulaObj formulaObj) {
        FormulaSettings settings = new FormulaSettings();
        if (BOOL_OPERATOR.contains(formulaObj.name)) {
            settings.dataType = BOOLEAN;
            settings.aggregation = NONE;
            settings.columnType = ATTRIBUTE;
        } else if (NUMERIC_OPERATOR.contains(formulaObj.name)) {
            settings.dataType = NUMERIC;
            settings.aggregation = ALL_AGGREGATION;
            settings.columnType = MEASURE;
        }
        return settings;
    }

    private static boolean isOperator(String value) {
        return NUMERIC_OPERATOR.contains(value);
    }

    private static int compareOperatorPriority(String type1, String type2) {
        if (LEVEL1.contains(type1) && LEVEL1.contains(type2)) {
            return 0;
        }
        if (LEVEL2.contains(type1) && LEVEL2.contains(type2)) {
            return 0;
        }
        if (LEVEL4.contains(type1) && LEVEL4.contains(type2)) {
            return 0;
        }
        if (LEVEL1.contains(type1)) {
            return 1;
        }
        if (LEVEL2.contains(type1) && !LEVEL1.contains(type2)) {
            return 1;
        }
        return -1;
    }

}
