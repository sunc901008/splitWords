package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationBuild;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<bool-columns> := <all-bool-column>;
public class BoolColInstruction {

    // 完整指令 bool-columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);

        JSONObject json = build(focusPhrase, formulas);
        String type = json.getString("type");
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            Column column = (Column) json.get("column");
            json1.put("column", column.getColumnId());
            // 根据列类型发送指令
            if (Constant.ColumnType.MEASURE.equals(column.getColumnType())) {
                json1.put("instId", "add_column_for_measure");
            } else {
                json1.put("instId", "add_column_for_group");
            }
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

    // 其他指令的一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        JSONObject res = new JSONObject();
        if ("<all-bool-column>".equals(node.getValue())) {
            JSONObject json = ColumnInstruction.build(node.getChildren());
            if (json.getBoolean("hasTable")) {
                res.put("type", Constant.InstType.TABLE_COLUMN);
            } else {
                res.put("type", Constant.InstType.COLUMN);
            }
            res.put("column", json.get("column"));
            return res;
        }
        throw new InvalidRuleException("Build instruction fail!!!");
    }
}
