package focus.search.instruction.functionInst.numberFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.nodeArgs.NumberOrNumColInst;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<max-function> := max ( <all-date-column> ) |
//        max ( <number-columns> ) |
//        max ( <number> );

//<min-function> := min ( <all-date-column> ) |
//        min ( <number-columns> ) |
//        min ( <number> );
public class MaxMinFuncInstruction {

    // 完整指令 max / min
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");

        json1.put("expression", arg(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        json2.put("content", AnnotationBuild.build(focusPhrase, index, amb));

        instructions.add(json2);

        return instructions;
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode param = focusPhrase.getFocusNodes().get(2);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        if ("<all-date-column>".equals(param.getValue())) {
            JSONObject arg1 = new JSONObject();
            JSONObject json = ColumnInstruction.build(param.getChildren());
            arg1.put("type", Constant.InstType.COLUMN);
            arg1.put("column", ((Column) json.get("column")).getColumnId());
            args.add(arg1);
        } else {
            args.add(NumberOrNumColInst.arg(param, formulas));
        }

        expression.put("args", args);

        return expression;
    }

}
