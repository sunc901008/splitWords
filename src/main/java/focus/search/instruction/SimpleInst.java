package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusNodeDetail;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;

/**
 * user: sunc
 * data: 2018/1/27.
 */
class SimpleInst extends CommonFunc {

    static JSONArray simpleFilter(FocusPhrase focusPhrase, int index) throws InvalidRuleException {
        if (focusPhrase.size() == 1) {
            return singleCol(focusPhrase.getLastNode(), index);
        }

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_simple_filter");
        int flag = 0;
        FocusNodeDetail fnd = focusPhrase.getNode(flag++).getDetails().get(0);
        if (fnd.type.equals(Constant.FNDType.TABLE)) {
            fnd = focusPhrase.getNode(flag++).getDetails().get(0);
        }
        json1.put("column", fnd.columnId);
        json1.put("operator", focusPhrase.getNode(flag++).getValue());
        FocusNode node = focusPhrase.getNode(flag);
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

    static JSONArray singleCol(FocusNode fn, int index) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_column_for_measure");
        FocusNodeDetail fnd = fn.getDetails().get(0);
        json1.put("column", fnd.columnId);
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        instructions.add(json2);
        return instructions;
    }

}
