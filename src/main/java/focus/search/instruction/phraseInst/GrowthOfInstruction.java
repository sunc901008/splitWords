package focus.search.instruction.phraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.sourceInst.AllColumnsInstruction;
import focus.search.instruction.sourceInst.NumberColInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<growth-of> := growth of <number-columns> by <date-columns> |
//              growth of <number-columns> by <date-columns> <year-over-year> |
//              growth of <number-columns> by <date-columns> <growth-of-by-date-interval> |
//              growth of <number-columns> by <date-columns> <growth-of-by-date-interval> <year-over-year>;
public class GrowthOfInstruction {

    // todo
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression_for_sort");

        FocusNode param = focusPhrase.getFocusNodes().get(2);
        json1.put("expression", AllColumnsInstruction.arg(param.getChildren(), formulas));
        String sortOrder = null;
        if (focusNodes.size() == 4) {
            sortOrder = focusNodes.get(3).getValue();
        }
        json1.put("sortOrder", sortOrder);

        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        // annotation content
        json2.put("content", AnnotationBuild.build(focusPhrase, index, amb));
        instructions.add(json2);

        instructions.addAll(NumberColInstruction.build(param.getChildren(), index + 1, amb, formulas));

        return instructions;

    }

}
