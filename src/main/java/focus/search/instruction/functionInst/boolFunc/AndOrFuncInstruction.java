package focus.search.instruction.functionInst.boolFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.BoolFuncColInstruction;
import focus.search.instruction.nodeArgs.NoOrAndBoolFuncColInstruction;
import focus.search.instruction.sourceInst.BoolColInstruction;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
//<and-function> := <bool-columns> and <bool-function-column> |
//        <bool-columns> and <and-function> |
//        <bool-columns> and <or-function> |
//        <no-or-and-bool-function-column> and <bool-function-column> |
//        <no-or-and-bool-function-column> and <no-or-and-bool-function-column>;

//<or-function> := <bool-columns> or <bool-function-column> |
//        <bool-columns> or <and-function> |
//        <bool-columns> or <or-function> |
//        <no-or-and-bool-function-column> or <bool-function-column> |
//        <no-or-and-bool-function-column> or <no-or-and-bool-function-column>;
public class AndOrFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        switch (first.getValue()) {
            case "<bool-columns>":
                return allBoolColBuild(focusPhrase, index, amb, formulas);
            case "<no-or-and-bool-function-column>":
                return noOrAndBoolFuncColBuild(focusPhrase, index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        switch (first.getValue()) {
            case "<bool-columns>":
                return allBoolColBuild(focusPhrase, formulas);
            case "<no-or-and-bool-function-column>":
                return noOrAndBoolFuncColBuild(focusPhrase, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        switch (first.getValue()) {
            case "<bool-columns>":
                return allBoolColBuildTokens(focusPhrase, formulas, amb);
            case "<no-or-and-bool-function-column>":
                return noOrAndBoolFuncColBuildTokens(focusPhrase, formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    //    <bool-columns> and <bool-function-column>
    //    <bool-columns> and <and-function>
    //    <bool-columns> and <or-function>

    //    <bool-columns> or <bool-function-column>
    //    <bool-columns> or <and-function>
    //    <bool-columns> or <or-function>
    // 完整指令
    private static JSONArray allBoolColBuild(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);

        json1.put("expression", allBoolColBuild(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        datas.addTokens(allBoolColBuildTokens(focusPhrase, formulas, amb));
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    private static JSONObject allBoolColBuild(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(0);
        FocusNode symbol = focusNodes.get(1);
        FocusNode param2 = focusNodes.get(2);

        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", symbol.getChildren().getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        JSONObject arg1 = new JSONObject();
        JSONObject json = ColumnInstruction.build(param1.getChildren());
        arg1.put("type", Constant.InstType.COLUMN);
        arg1.put("column", ((Column) json.get("column")).getColumnId());
        args.add(arg1);

        if ("<bool-function-column>".equals(param2.getValue())) {
            args.add(BoolFuncColInstruction.arg(param2.getChildren(), formulas));
        } else if ("<and-function>".equals(param2.getValue())) {
            args.add(arg(param2.getChildren(), formulas));
        } else if ("<or-function>".equals(param2.getValue())) {
            args.add(arg(param2.getChildren(), formulas));
        }

        expression.put("args", args);
        return expression;
    }

    private static List<AnnotationToken> allBoolColBuildTokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<AnnotationToken> tokens = new ArrayList<>();
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(0);
        FocusNode symbol = focusNodes.get(1);
        FocusNode param2 = focusNodes.get(2);

        tokens.addAll(BoolColInstruction.tokens(param1, formulas, amb));

        AnnotationToken token2 = new AnnotationToken();
        token2.value = symbol.getValue();
        token2.type = Constant.AnnotationTokenType.SYMBOL;
        token2.begin = symbol.getBegin();
        token2.end = symbol.getEnd();
        tokens.add(token2);

        if ("<bool-function-column>".equals(param2.getValue())) {
            tokens.addAll(BoolFuncColInstruction.tokens(param2.getChildren(), formulas, amb));
        } else if ("<and-function>".equals(param2.getValue()) || "<or-function>".equals(param2.getValue())) {
            tokens.addAll(tokens(param2.getChildren(), formulas, amb));
        }

        return tokens;
    }

    //    <no-or-and-bool-function-column> and <no-or-and-bool-function-column>

    //    <no-or-and-bool-function-column> or <no-or-and-bool-function-column>
    // 完整指令
    private static JSONArray noOrAndBoolFuncColBuild(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);

        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression");

        json1.put("expression", noOrAndBoolFuncColBuild(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        datas.addTokens(noOrAndBoolFuncColBuildTokens(focusPhrase, formulas, amb));
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    public static JSONObject noOrAndBoolFuncColBuild(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(0);
        FocusNode param2 = focusPhrase.getFocusNodes().get(2);
        FocusNode symbol = focusPhrase.getFocusNodes().get(1);

        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", symbol.isHasChild() ? symbol.getChildren().getNodeNew(0).getValue() : symbol.getValue());
        JSONArray args = new JSONArray();

        args.add(NoOrAndBoolFuncColInstruction.arg(param1.getChildren(), formulas));
        args.add(NoOrAndBoolFuncColInstruction.arg(param2.getChildren(), formulas));

        expression.put("args", args);
        return expression;
    }

    // annotation token
    public static List<AnnotationToken> noOrAndBoolFuncColBuildTokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<AnnotationToken> tokens = new ArrayList<>();
        FocusNode param1 = focusPhrase.getFocusNodes().get(0);
        FocusNode param2 = focusPhrase.getFocusNodes().get(2);
        FocusNode symbol = focusPhrase.getFocusNodes().get(1);

        tokens.addAll(NoOrAndBoolFuncColInstruction.tokens(param1.getChildren(), formulas, amb));

        AnnotationToken token2 = new AnnotationToken();
        token2.value = symbol.getValue();
        token2.type = Constant.AnnotationTokenType.SYMBOL;
        token2.begin = symbol.getBegin();
        token2.end = symbol.getEnd();
        tokens.add(token2);

        tokens.addAll(NoOrAndBoolFuncColInstruction.tokens(param2.getChildren(), formulas, amb));

        return tokens;
    }

}
