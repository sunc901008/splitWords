package focus.search.instruction.functionInst.boolFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
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
//<not-function> := not ( <bool-columns> ) |
//        not ( <bool-function-column> );
public class NotFuncInstruction {

    // 完整指令 not
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

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode param = focusPhrase.getFocusNodes().get(2);

        JSONObject arg = new JSONObject();
        arg.put("type", Constant.InstType.FUNCTION);
        arg.put("name", focusPhrase.getNodeNew(0).getValue());
        arg.put("args", BoolColOrBoolFuncColInst.arg(param, formulas));
        return arg;
    }

}
