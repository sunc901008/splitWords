package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;

/**
 * user: sunc
 * data: 2018/1/27.
 */
class SimpleInst {

    static JSONArray simpleFilter(FocusPhrase focusPhrase, int index, JSONObject amb) throws InvalidRuleException {
        if ((focusPhrase.getFirstNode().getType().equals(Constant.FNDType.TABLE) && focusPhrase.getLastNode().getType().equals(Constant.FNDType
                .COLUMN)) || focusPhrase.size() == 1) {
            return singleCol(focusPhrase, index, amb);
        }

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_simple_filter");
        int flag = 0;
        FocusNode columnNode = focusPhrase.getNode(flag++);
        if (columnNode.getType().equals(Constant.FNDType.TABLE)) {
            columnNode = focusPhrase.getNode(flag++);
        }
        json1.put("column", columnNode.getColumn().getColumnId());
        FocusNode operatorNode = focusPhrase.getNode(flag++);
        json1.put("operator", operatorNode.getValue());
        FocusNode numberNode = focusPhrase.getNode(flag);
        if (numberNode.getType().equalsIgnoreCase("integer")) {
            json1.put("value", Integer.parseInt(numberNode.getValue()));
        } else if (numberNode.getType().equalsIgnoreCase("number")) {
            json1.put("value", Double.parseDouble(numberNode.getValue()));
        }
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        json2.put("content", AnnotationBuild.build(focusPhrase, index, amb));

        instructions.add(json2);

        return instructions;
    }

    static JSONArray singleCol(FocusPhrase focusPhrase, int index, JSONObject amb) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_column_for_measure");
        json1.put("column", focusPhrase.getLastNode().getColumn().getColumnId());
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        json2.put("content", AnnotationBuild.build(focusPhrase, index, amb));
        instructions.add(json2);
        return instructions;
    }

}
