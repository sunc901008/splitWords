package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;

import java.util.List;

/**
 * user: sunc
 * data: 2018/1/27.
 */
class SimpleInst extends CommonFunc {

    static JSONArray simpleFilter(FocusPhrase focusPhrase, int index, List<SourceReceived> srs) throws InvalidRuleException {
        if (focusPhrase.size() == 1) {
            return singleCol(focusPhrase.getLastNode().getValue(), index, srs);
        }

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_simple_filter");
        ColumnReceived col = getCol(focusPhrase.getNode(0).getValue(), srs);
        if (col == null) {
            throw new InvalidRuleException("Build instruction fail!!!");
        }
        json1.put("column", col.columnId);
        json1.put("operator", focusPhrase.getNode(1).getValue());
        FocusNode node = focusPhrase.getNode(2);
        if (node.getType().equalsIgnoreCase("integer")) {
            json1.put("value", Integer.parseInt(node.getValue()));
        } else if (node.getType().equalsIgnoreCase("number")) {
            json1.put("value", Double.parseDouble(node.getValue()));
        }
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        instructions.add(json2);

        return instructions;
    }

    static JSONArray singleCol(String colName, int index, List<SourceReceived> srs) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_column_for_measure");
        ColumnReceived col = getCol(colName, srs);
        if (col == null) {
            throw new InvalidRuleException("Build instruction fail!!!");
        }
        json1.put("column", col.columnId);
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        instructions.add(json2);
        return instructions;
    }

}
