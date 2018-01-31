package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
class TopNInst extends CommonFunc {

    static JSONArray build(FocusPhrase focusPhrase, int index) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "set_top_n");
        if (focusPhrase.size() == 2) {
            json1.put("n", 1);
        } else if (focusPhrase.size() == 3) {
            json1.put("n", Integer.parseInt(focusPhrase.getNode(1).getValue()));
        } else {
            throw new InvalidRuleException("Build instruction fail!!!");
        }
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        instructions.add(json2);

        instructions.addAll(SimpleInst.singleCol(focusPhrase.getLastNode().getValue(), index + 1));

        return instructions;
    }

}