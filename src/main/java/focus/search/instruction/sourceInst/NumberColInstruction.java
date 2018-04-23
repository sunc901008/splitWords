package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.functionInst.NumberFuncInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<number-columns> := <all-int-column> |
//        <all-double-column> |
//        <number-function-column>;
public class NumberColInstruction {

    // 完整指令 columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression");

        JSONObject expression = new JSONObject();
        JSONObject json = build(focusPhrase, formulas);
        String type = json.getString("type");
        // todo
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            expression.put("type", "column");
            Column column = (Column) json.get("column");
            expression.put("value", column.getColumnId());
            if (Constant.ColumnType.MEASURE.equals(column.getColumnType())) {
                json1.put("instId", "add_column_for_measure");
            } else {
                json1.put("instId", "add_column_for_group");
            }
        } else if (Constant.InstType.FUNCTION.equals(type)) {
            expression = json.getJSONObject(Constant.InstType.FUNCTION);
        }

        json1.put("expression", expression);
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        json2.put("content", AnnotationBuild.build(focusPhrase, index, amb));

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        JSONObject res = new JSONObject();
        switch (node.getValue()) {
            case "<all-int-column>":
            case "<all-double-column>":
                JSONObject json = ColumnInstruction.build(node.getChildren());
                if (json.getBoolean("hasTable")) {
                    res.put("type", Constant.InstType.TABLE_COLUMN);
                } else {
                    res.put("type", Constant.InstType.COLUMN);
                }
                res.put("column", json.get("column"));
                return res;
            case "<number-function-column>":
                res.put("type", Constant.InstType.FUNCTION);
                res.put("function", NumberFuncInstruction.arg(node.getChildren(), formulas));
                return res;
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }
}
