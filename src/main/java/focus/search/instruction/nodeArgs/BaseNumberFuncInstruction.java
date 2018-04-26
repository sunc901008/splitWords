package focus.search.instruction.nodeArgs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationBuild;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.NumberFuncInstruction;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

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
//        <no-number-function-column> <math-symbol> <number>;
public class BaseNumberFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression");

        json1.put("expression", arg(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);
        datas.addTokens(tokens(focusPhrase, formulas, amb));
        json2.put("content", datas);

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
            JSONObject arg1 = new JSONObject();
            JSONObject json = ColumnInstruction.build(param1.getChildren());
            arg1.put("type", Constant.InstType.COLUMN);
            arg1.put("column", ((Column) json.get("column")).getColumnId());
            args.add(arg1);
        } else if ("<no-number-function-column>".equals(param1.getValue())) {
            args.add(NumberFuncInstruction.arg(param1.getChildren(), formulas));
        }
        args.add(NumberOrNumColInst.arg(param2, formulas));

        expression.put("args", args);
        return expression;

    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws InvalidRuleException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(0);
        FocusNode param2 = focusPhrase.getFocusNodes().get(2);
        FocusNode symbol = focusPhrase.getFocusNodes().get(1).getChildren().getFirstNode();

        List<AnnotationToken> tokens = new ArrayList<>();
        if ("<number>".equals(param1.getValue())) {
            tokens.add(NumberArg.token(param1));
        } else if ("<number-source-column>".equals(param1.getValue())) {
            FocusPhrase fp = param1.getChildren();
            int begin = fp.getFirstNode().getBegin();
            int end = fp.getLastNode().getEnd();
            tokens.add(AnnotationToken.singleCol(fp.getLastNode().getColumn(), fp.size() == 2, begin, end, amb));
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
