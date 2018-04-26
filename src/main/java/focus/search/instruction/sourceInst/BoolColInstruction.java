package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.ArrayList;
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
        json1.put("instId", "add_expression");
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.ATTRIBUTE_COLUMN);

        JSONObject expression = new JSONObject();
        JSONObject json = build(focusPhrase, formulas);
        String type = json.getString("type");
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            Column column = (Column) json.get("column");
            expression.put("type", Constant.InstType.COLUMN);
            expression.put("value", column.getColumnId());
            int begin = focusPhrase.getFirstNode().getBegin();
            int end = focusPhrase.getLastNode().getEnd();
            datas.addToken(AnnotationToken.singleCol(column, Constant.InstType.TABLE_COLUMN.equals(type), begin, end, amb));
        }
        json1.put("expression", expression);
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        json2.put("content", datas);

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

    // annotation token
    public static List<AnnotationToken> tokens(FocusNode focusNode, List<Formula> formulas, JSONObject amb) throws InvalidRuleException {
        List<AnnotationToken> tokens = new ArrayList<>();
        FocusPhrase fp = focusNode.getChildren();
        int begin = fp.getFirstNode().getBegin();
        int end = fp.getLastNode().getEnd();
        tokens.add(AnnotationToken.singleCol(fp.getLastNode().getColumn(), fp.size() == 2, begin, end, amb));
        return tokens;
    }

}
