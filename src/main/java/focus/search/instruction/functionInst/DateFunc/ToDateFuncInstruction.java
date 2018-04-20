package focus.search.instruction.functionInst.DateFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.nodeArgs.NumberOrNumColInst;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */
//<to_date-function> := to_date ( <number-columns> ) |
//        to_date ( <number> );
public class ToDateFuncInstruction {

    // 完整指令 to_date
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

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode param = focusPhrase.getFocusNodes().get(2);
        JSONObject arg = new JSONObject();
        arg.put("type", Constant.InstType.FUNCTION);
        arg.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        args.add(NumberOrNumColInst.arg(param, formulas));

        arg.put("args", args);
        return arg;
    }


}
