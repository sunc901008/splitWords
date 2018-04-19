package focus.search.instruction.functionInst.boolFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.functionInst.BoolFuncColInstruction;
import focus.search.instruction.nodeArgs.NoOrAndBoolFuncColInstruction;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
//<and-function> := <all-bool-column> and <bool-function-column> |
//        <all-bool-column> and <and-function> |
//        <all-bool-column> and <or-function> |
//        <no-or-and-bool-function-column> and <bool-function-column> |
//        <no-or-and-bool-function-column> and <no-or-and-bool-function-column>;

//<or-function> := <all-bool-column> or <bool-function-column> |
//        <all-bool-column> or <and-function> |
//        <all-bool-column> or <or-function> |
//        <no-or-and-bool-function-column> or <bool-function-column> |
//        <no-or-and-bool-function-column> or <no-or-and-bool-function-column>;
public class AndOrFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        switch (first.getValue()) {
            case "<all-bool-column>":
                return allBoolColBuild(focusPhrase, index, amb, formulas);
            case "<no-or-and-bool-function-column>":
                return noOrAndBoolFuncColBuild(focusPhrase, index, amb, formulas);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

    // 其他指令的一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        return null;
    }

    //    <all-bool-column> and <bool-function-column>
    //    <all-bool-column> and <and-function>
    //    <all-bool-column> and <or-function>

    //    <all-bool-column> or <bool-function-column>
    //    <all-bool-column> or <and-function>
    //    <all-bool-column> or <or-function>
    private static JSONArray allBoolColBuild(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");

        JSONObject expression = new JSONObject();
        expression.put("type", "function");
        expression.put("name", focusNodes.get(1).getChildren().getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        JSONObject arg1 = new JSONObject();
        JSONObject json = ColumnInstruction.build(focusNodes.get(0).getChildren());
        arg1.put("type", Constant.InstType.COLUMN);
        arg1.put("column", ((Column) json.get("column")).getColumnId());
        args.add(arg1);

        FocusNode third = focusNodes.get(2);
        if ("<bool-function-column>".equals(third.getValue())) {
            args.add(BoolFuncColInstruction.build(third.getChildren(), formulas));
        } else if ("<and-function>".equals(third.getValue())) {
            args.add(build(third.getChildren(), formulas));
        } else if ("<or-function>".equals(third.getValue())) {
            args.add(build(third.getChildren(), formulas));
        }

        expression.put("args", args);
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

    //    <no-or-and-bool-function-column> and <bool-function-column>
    //    <no-or-and-bool-function-column> and <no-or-and-bool-function-column>

    //    <no-or-and-bool-function-column> or <bool-function-column>
    //    <no-or-and-bool-function-column> or <no-or-and-bool-function-column>
    private static JSONArray noOrAndBoolFuncColBuild(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");

        JSONObject expression = new JSONObject();
        expression.put("type", "function");
        expression.put("name", focusPhrase.getFocusNodes().get(1).getChildren().getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        FocusNode param1 = focusPhrase.getFocusNodes().get(0);
        FocusNode param2 = focusPhrase.getFocusNodes().get(2);
        args.add(NoOrAndBoolFuncColInstruction.arg(param1.getChildren(), formulas));
        if ("<bool-function-column>".equals(param2.getValue())) {
            args.add(BoolFuncColInstruction.build(param2.getChildren(), formulas));
        } else if ("<no-or-and-bool-function-column>".equals(param2.getValue())) {
            args.add(NoOrAndBoolFuncColInstruction.arg(param2.getChildren(), formulas));
        }
        expression.put("args", args);
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

}
