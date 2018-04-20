package focus.search.instruction.phraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.sourceInst.NumberColInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<top-n> := top <integer> <number-columns> |
//        top <number-columns>;
//
//<bottom-n> := bottom <integer> <number-columns> |
//        bottom <number-columns>;
public class TopBottomInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        int flag = 0;
        int n = 1;
        FocusNode keyword = focusPhrase.getFocusNodes().get(flag++);
        if ("top".equals(keyword.getValue())) {
            json1.put("instId", "set_top_n");
        } else if ("bottom".equals(keyword.getValue())) {
            json1.put("instId", "set_bottom_n");
        }
        FocusNode integer = focusPhrase.getFocusNodes().get(flag++);
        FocusNode param;
        if ("<integer>".equals(integer.getValue())) {
            n = Integer.parseInt(integer.getChildren().getNodeNew(0).getValue());
            param = focusPhrase.getFocusNodes().get(flag);
        } else {
            param = integer;
        }
        json1.put("n", n);
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
