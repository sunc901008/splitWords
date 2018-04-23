package focus.search.instruction.functionInst.StringFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.instruction.sourceInst.DateColInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<month-function> := month ( <date-columns> ) |
//        month ( <column-value> );
public class MonthFuncInstruction {

    // 完整指令 month
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
        FocusNode param = focusPhrase.getFocusNodes().get(2);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();
        if ("<date-columns>".equals(param.getValue())) {
            args.add(DateColInstruction.arg(param.getChildren(), formulas));
        } else if ("<column-value>".equals(param.getValue())) {
            args.add(ColumnValueInstruction.arg(param));
        }

        expression.put("args", args);

        return expression;
    }

}
