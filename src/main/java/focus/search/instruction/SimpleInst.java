package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Column;
import focus.search.response.search.AnnotationResponse;

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
        FocusNode tableNode = null;
        FocusNode columnNode = focusPhrase.getNode(flag++);
        Column column = columnNode.getColumn();
        if (columnNode.getType().equals(Constant.FNDType.TABLE)) {
            tableNode = columnNode;
            column = focusPhrase.getNode(flag++).getColumn();
        }
        json1.put("column", column.getColumnId());
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

        // todo annotation content
        AnnotationResponse.Datas datas = new AnnotationResponse.Datas();
        AnnotationResponse.Tokens token1 = new AnnotationResponse.Tokens();
        token1.description = column.getSourceName() + " " + column.getColumnDisplayName();
        token1.columnId = column.getColumnId();
        token1.columnName = column.getColumnDisplayName();
        token1.detailType = column.getDataType();
        token1.type = column.getColumnType();
        token1.value = column.getColumnDisplayName();
        if (tableNode != null) {
            token1.tokens.add(column.getSourceName());
        }
        token1.tokens.add(column.getColumnDisplayName());

        json2.put("content", datas);

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
        json1.put("column", fn.getColumn().getColumnId());
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        instructions.add(json2);
        return instructions;
    }

}
