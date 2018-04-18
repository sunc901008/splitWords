package focus.search.instruction.functionInst.boolFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.sourceInst.StringColInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
//<contains-function> := contains ( <string-columns> , <string-columns> ) |
//        contains ( <column-value> , <string-columns> ) |
//        contains ( <string-columns> , <column-value> ) |
//        contains ( <column-value> , <column-value> );
public class ContainsFuncInstruction {

    // 完整指令 to_bool
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(2);
        FocusNode param2 = focusPhrase.getFocusNodes().get(4);

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");

        JSONObject expression = new JSONObject();
        expression.put("type", "function");
        expression.put("name", "contains");
        JSONArray args = new JSONArray();
        JSONObject arg1 = new JSONObject();
        if (param1.getValue().equals("<string-columns>")) {
            StringColInstruction.build(param1.getChildren(), formulas);
        } else {// 列中值

        }
        args.add(arg1);

        JSONObject arg2 = new JSONObject();
        if (param2.getValue().equals("<string-columns>")) {

        } else {// 列中值

        }
        args.add(arg2);
        expression.put("args", args);
        json1.put("expression", expression);
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
    public static JSONArray build(FocusPhrase focusPhrase, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        return null;
    }

}
