package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.FormulaAnalysis;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.NumberFuncInstruction;
import focus.search.instruction.sourceInst.FormulaColumnInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.ArrayList;
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
//        <no-number-function-column> <math-symbol> <number> |
//        <number>;
public class BaseNumberFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_EXPRESSION);

        json1.put("expression", arg(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);

        // annotation content
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);
        datas.addTokens(tokens(focusPhrase, formulas, amb));
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;

    }

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        if (focusPhrase.getFocusNodes().size() == 1) {//<number>
            JSONObject expression = new JSONObject();
            expression.put("type", "number");
            expression.put("value", focusPhrase.getFirstNode().getValue());
            return expression;
        }

        FormulaAnalysis.FormulaObj formulaObj = FormulaAnalysis.numberAnalysis(focusPhrase, formulas);

        JSONObject expression = new JSONObject();
        expression.put("type", formulaObj.type);
        expression.put("name", formulaObj.name);
        JSONArray args = new JSONArray();
        args.addAll(formulaObj.args);

        expression.put("args", args);
        return expression;

    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<FocusNode> nodes = focusPhrase.getFocusNodes();

        List<AnnotationToken> tokens = new ArrayList<>();

        if (nodes.size() == 1) {//<number>
            tokens.add(NumberArg.token(focusPhrase.getFirstNode()));
            return tokens;
        }

        FocusNode param1 = nodes.get(0);
        FocusNode param2 = nodes.get(2);
        FocusNode symbol = nodes.get(1).getChildren().getFirstNode();

        if ("<number>".equals(param1.getValue())) {
            tokens.add(NumberArg.token(param1));
        } else if ("<number-source-column>".equals(param1.getValue())) {
            FocusPhrase fp = param1.getChildren();
            if ("<number-formula-column>".equals(fp.getFocusNodes().get(0).getValue())) {
                tokens.addAll(FormulaColumnInstruction.tokens(fp.getFocusNodes().get(0).getChildren(), formulas));
            } else {
                int begin = fp.getFirstNode().getBegin();
                int end = fp.getLastNode().getEnd();
                tokens.add(AnnotationToken.singleCol(fp.getLastNode().getColumn(), fp.size() == 2, begin, end, amb));
            }
        } else if ("<no-number-function-column>".equals(param1.getValue())) {
            tokens.addAll(NumberFuncInstruction.tokens(param1.getChildren(), formulas, amb));
        }

        AnnotationToken token2 = new AnnotationToken();
        token2.value = symbol.getValue();
        token2.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token2.begin = symbol.getBegin();
        token2.end = symbol.getEnd();
        tokens.add(token2);

        tokens.addAll(NumberOrNumColInst.tokens(param2, formulas, amb));

        return tokens;
    }

}
