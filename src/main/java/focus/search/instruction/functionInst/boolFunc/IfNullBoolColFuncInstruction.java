package focus.search.instruction.functionInst.boolFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.nodeArgs.BoolColOrBoolFuncColInst;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
//<ifnull-bool-column-function> := ifnull ( <bool-columns> , <bool-columns> ) |
//        ifnull ( <bool-function-column> , <bool-columns> ) |
//        ifnull ( <bool-columns> , <bool-function-column> ) |
//        ifnull ( <bool-function-column> , <bool-function-column> );
public class IfNullBoolColFuncInstruction {

    // 完整指令
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


    // 其他指令一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(2);
        FocusNode param2 = focusPhrase.getFocusNodes().get(3);
        JSONObject expression = new JSONObject();
        expression.put("type", "function");
        expression.put("name", "ifnull");
        JSONArray args = new JSONArray();
        args.add(BoolColOrBoolFuncColInst.arg(param1, formulas));
        args.add(BoolColOrBoolFuncColInst.arg(param2, formulas));
        expression.put("args", args);

        return expression;
    }

}
