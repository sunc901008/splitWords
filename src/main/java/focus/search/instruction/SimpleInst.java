package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Column;

/**
 * user: sunc
 * data: 2018/1/27.
 */
class SimpleInst extends CommonFunc {

    static JSONArray simpleFilter(FocusPhrase focusPhrase, int index) throws InvalidRuleException {
        if (focusPhrase.size() == 1) {
            return singleCol(focusPhrase.getLastNode().getValue(), index);
        }

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_simple_filter");
        Column col = getCol(focusPhrase.getNode(0).getValue());
        if (col == null) {
            throw new InvalidRuleException("Build instruction fail!!!");
        }
        json1.put("column", col.getId());
        json1.put("operator", focusPhrase.getNode(1).getValue());
        FocusToken t = focusPhrase.getNode(2).getFt();
        if (t.getType().equalsIgnoreCase("integer")) {
            json1.put("value", Integer.parseInt(t.getWord()));
        } else if (t.getType().equalsIgnoreCase("number")) {
            json1.put("value", Double.parseDouble(t.getWord()));
        }
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        instructions.add(json2);

        return instructions;
    }

    static JSONArray singleCol(String colName, int index) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_column_for_measure");
        Column col = getCol(colName);
        if (col == null) {
            throw new InvalidRuleException("Build instruction fail!!!");
        }
        json1.put("column", col.getId());
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        instructions.add(json2);
        return instructions;
    }

}
