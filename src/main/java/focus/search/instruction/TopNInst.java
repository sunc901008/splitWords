package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
class TopNInst {

    static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "set_top_n");
        FocusNode fn = focusPhrase.getNode(1);
        if (fn.getType().equals(Constant.FNDType.INTEGER)) {
            json1.put("n", Integer.parseInt(fn.getValue()));
        } else {
            json1.put("n", 1);
        }
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        instructions.add(json2);

        instructions.addAll(SimpleInst.singleCol(focusPhrase, index + 1, amb, formulas));

        return instructions;
    }

}