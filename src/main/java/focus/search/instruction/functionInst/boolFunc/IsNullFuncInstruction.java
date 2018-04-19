package focus.search.instruction.functionInst.boolFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.instruction.sourceInst.AllColumnsInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
//<isnull-function> := isnull ( <all-columns> ) |
//        isnull ( <number> );
public class IsNullFuncInstruction {

    // 完整指令 isnull
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");

        json1.put("expression", build(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        json2.put("content", AnnotationBuild.build(focusPhrase, index, amb));

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode param = focusPhrase.getFocusNodes().get(2);

        JSONObject arg = new JSONObject();
        arg.put("type", "function");
        arg.put("name", "isnull");
        JSONArray args = new JSONArray();

        if ("<all-columns>".equals(param.getValue())) {
            JSONObject json = AllColumnsInstruction.build(focusPhrase, formulas);
            String type = json.getString("type");
            JSONObject arg1 = new JSONObject();
            // todo
            if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
                arg1.put("type", "column");
                arg1.put("value", ((Column) json.get("column")).getColumnId());
            }
            args.add(arg1);
        } else if ("<number>".equals(param.getValue())) {
            args.add(NumberArg.arg(param));
        }
        arg.put("args", args);
        return arg;
    }

}
