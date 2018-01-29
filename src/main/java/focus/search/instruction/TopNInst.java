package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Column;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
public class TopNInst extends CommonFunc {

    public static JSONArray build(FocusPhrase focusPhrase, int index) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId01 = new JSONArray();
        annotationId01.add(index);
        JSONObject json01 = new JSONObject();
        json01.put("annotationId", annotationId01);
        json01.put("instId", "set_top_n");
        if (focusPhrase.size() == 2) {
            json01.put("n", 1);
        } else if (focusPhrase.size() == 3) {
            json01.put("n", Integer.parseInt(focusPhrase.getNode(1).getValue()));
        } else {
            throw new InvalidRuleException("Build instruction fail!!!");
        }
        instructions.add(json01);
        JSONObject json02 = new JSONObject();
        json02.put("annotationId", annotationId01);
        json02.put("instId", "annotation");
        instructions.add(json02);


        JSONArray annotationId11 = new JSONArray();
        annotationId11.add(index + 1);
        JSONObject json11 = new JSONObject();
        json11.put("annotationId", annotationId11);
        json11.put("instId", "add_column_for_measure");
        Column col = getCol(focusPhrase.getLastNode().getValue());
        if (col == null) {
            throw new InvalidRuleException("Build instruction fail!!!");
        }
        json11.put("column", col.getId());
        instructions.add(json11);
        JSONObject json12 = new JSONObject();
        json12.put("annotationId", annotationId11);
        json12.put("instId", "annotation");
        instructions.add(json12);

        return instructions;
    }

}