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
 * user: sunc
 * data: 2018/1/27.
 */
class SimpleInst {

    static JSONArray simpleFilter(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        if ((focusPhrase.getFirstNode().getType().equals(Constant.FNDType.TABLE) && focusPhrase.getLastNode().getType().equals(Constant.FNDType
                .COLUMN)) || focusPhrase.size() == 1) {
            return singleCol(focusPhrase, index, amb, formulas);
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
        if (numberNode.getType().equalsIgnoreCase(Constant.FNDType.INTEGER)) {
            json1.put("value", Integer.parseInt(numberNode.getValue()));
        } else if (numberNode.getType().equalsIgnoreCase(Constant.FNDType.DOUBLE)) {
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

    static JSONArray singleCol(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode node = focusPhrase.getLastNode();
        if (node.getType().equals(Constant.FNDType.COLUMN)) {
            return column(focusPhrase, index, amb);
        } else {
            Formula formula = CommonFunc.getFormula(formulas, node.getValue());
            return formula(focusPhrase, index, amb, formula);
        }
    }

    private static JSONArray column(FocusPhrase focusPhrase, int index, JSONObject amb) throws InvalidRuleException {
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

    private static JSONArray formula(FocusPhrase focusPhrase, int index, JSONObject amb, Formula formula) throws InvalidRuleException {
        FocusNode node = focusPhrase.getLastNode();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression");
        json1.put("name", node.getValue());
        json1.put("category", Constant.CategoryType.EXPRESSION);

        assert formula != null;
        json1.put("type", formula.getColumnType());
        json1.put("aggregation", formula.getAggregation());
        json1.put("expression", formula.getInstruction());

        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
        json2.put("content", AnnotationBuild.build(focusPhrase, index, amb, formula));
        instructions.add(json2);
        return instructions;
    }

}
