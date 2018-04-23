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

//<number-columns> <bool-symbol> <number>
//<number-columns> <bool-symbol> <number-columns>
//<number> <bool-symbol> <number-columns>
//<number> <bool-symbol> <number>

public class FilterNumOrNumColInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(0);
        FocusNode symbol = focusNodes.get(1);
        FocusNode param2 = focusNodes.get(2);

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");

        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        JSONArray args = new JSONArray();

        expression.put("name", symbol.getChildren().getNodeNew(0).getValue());
        args.add(NumberOrNumColInst.arg(param1, formulas));
        args.add(NumberOrNumColInst.arg(param2, formulas));

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
