package focus.search.instruction.functionInst.StringFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationBuild;
import focus.search.instruction.nodeArgs.BoolColOrBoolFuncColInst;
import focus.search.instruction.nodeArgs.ColValueOrStringColInst;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<if-then-else-string-function> := if <bool-columns> then <column-value> else <column-value> |
//        if <bool-function-column> then <column-value> else <column-value>|
//        if <bool-columns> then <string-columns> else <string-columns> |
//        if <bool-function-column> then <string-columns> else <string-columns>;
public class IfThenElseStringColFuncInstruction {

    // 完整指令 if-expression
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression");

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
        FocusNode param1 = focusPhrase.getFocusNodes().get(1);
        FocusNode param2 = focusPhrase.getFocusNodes().get(3);
        FocusNode param3 = focusPhrase.getFocusNodes().get(5);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", "if-expression");
        JSONArray args = new JSONArray();
        args.add(BoolColOrBoolFuncColInst.arg(param1, formulas));
        args.add(ColValueOrStringColInst.arg(param2, formulas));
        args.add(ColValueOrStringColInst.arg(param3, formulas));
        expression.put("args", args);

        return expression;
    }

}
