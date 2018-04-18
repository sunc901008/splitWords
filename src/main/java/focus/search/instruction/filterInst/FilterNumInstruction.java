package focus.search.instruction.filterInst;

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
 * date: 2018/4/17
 * description:
 */
//<number> <bool-symbol> <number-columns>
//<number> <bool-symbol> <number>
public class FilterNumInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");

        JSONObject expression = new JSONObject();
        expression.put("type", "");
        JSONArray args = new JSONArray();

        FocusNode first = focusPhrase.getFocusNodes().get(0).getChildren().getNodeNew(0);
        JSONObject arg1 = new JSONObject();
        arg1.put("type", "number");
        if (Constant.FNDType.INTEGER.equals(first.getType())) {
            arg1.put("value", Integer.parseInt(first.getValue()));
        } else {
            arg1.put("value", Float.parseFloat(first.getValue()));
        }
        args.add(arg1);

        FocusNode second = focusNodes.get(1);
        expression.put("name", second.getChildren().getNodeNew(0).getValue());

        FocusNode third = focusPhrase.getFocusNodes().get(2);
        args.add(NumberOrNumColInst.arg(third, formulas));
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

}
