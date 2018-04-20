package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.functionInst.NumberFuncInstruction;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<number-function> := <number> <math-symbol> <number-columns> |
//        <number> <math-symbol> <number> |
//        <number-source-column> <math-symbol> <number-columns> |
//        <number-source-column> <math-symbol> <number> |
//        <no-number-function-column> <math-symbol> <number-columns> |
//        <no-number-function-column> <math-symbol> <number>;
public class BaseNumberFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");

        json1.put("expression", arg(focusPhrase, formulas));
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
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(0);
        FocusNode param2 = focusPhrase.getFocusNodes().get(2);
        FocusNode symbol = focusPhrase.getFocusNodes().get(1);

        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", symbol.getChildren().getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        if ("<number>".equals(param1.getValue())) {
            args.add(NumberArg.arg(param1));
        } else if ("<number-source-column>".equals(param1.getValue())) {
            args.add(ColumnInstruction.build(param1.getChildren()));
        } else if ("<no-number-function-column>".equals(param1.getValue())) {
            args.add(NumberFuncInstruction.arg(param1.getChildren(), formulas));
        }
        args.add(NumberOrNumColInst.arg(param2, formulas));

        expression.put("args", args);
        return expression;

    }

}
