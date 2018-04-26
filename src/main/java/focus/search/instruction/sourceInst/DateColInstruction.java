package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.DateFuncInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<date-columns> := <all-date-column> |
//        <date-function-column>;
public class DateColInstruction {

    // 完整指令 columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression");
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE);

        String type = focusPhrase.getFocusNodes().get(0).getValue();
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            datas.category = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        } else if (Constant.InstType.FUNCTION.equals(type)) {
            datas.category = Constant.AnnotationCategory.EXPRESSION;
        }

        datas.addTokens(tokens(focusPhrase, formulas, amb));

        json1.put("expression", arg(focusPhrase, formulas));
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
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        JSONObject json = build(focusPhrase, formulas);
        String type = json.getString("type");
        JSONObject arg = new JSONObject();
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            arg.put("type", "column");
            arg.put("value", ((Column) json.get("column")).getColumnId());
        } else if (Constant.InstType.FUNCTION.equals(type)) {
            arg = json.getJSONObject(Constant.InstType.FUNCTION);
        }
        return arg;
    }

    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        JSONObject res = new JSONObject();
        switch (node.getValue()) {
            case "<all-date-column>":
                JSONObject json = ColumnInstruction.build(node.getChildren());
                if (json.getBoolean("hasTable")) {
                    res.put("type", Constant.InstType.TABLE_COLUMN);
                } else {
                    res.put("type", Constant.InstType.COLUMN);
                }
                res.put("column", json.get("column"));
                return res;
            case "<date-function-column>":
                res.put("type", Constant.InstType.FUNCTION);
                res.put("function", DateFuncInstruction.arg(node.getChildren(), formulas));
                return res;
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws InvalidRuleException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        switch (node.getValue()) {
            case "<all-date-column>":
                List<AnnotationToken> tokens = new ArrayList<>();
                tokens.add(AnnotationToken.singleCol(node.getChildren(), amb));
                return tokens;
            case "<date-function-column>":
                return DateFuncInstruction.tokens(node.getChildren(), formulas, amb);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
