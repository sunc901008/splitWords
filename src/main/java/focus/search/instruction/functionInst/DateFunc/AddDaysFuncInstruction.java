package focus.search.instruction.functionInst.DateFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.FormulaCase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.ColValueOrDateColInst;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.suggestions.SourcesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/28
 * description:
 */
//<add-days-function> := add_days ( <all-date-column> , <integer> ) |
//        add_days ( <date-string-value> , <integer> );
public class AddDaysFuncInstruction {

    // 完整指令 to_date
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);

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
        datas.addTokens(tokens(focusPhrase, formulas, amb));
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param = focusNodes.get(2);
        FocusNode integer = focusNodes.get(4);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        args.add(ColValueOrDateColInst.arg(param, formulas));
        args.add(NumberArg.arg(integer));

        expression.put("args", args);
        return expression;
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        List<AnnotationToken> tokens = new ArrayList<>();
        FocusNode param = focusNodes.get(2);
        FocusNode integer = focusNodes.get(4);
        AnnotationToken token1 = new AnnotationToken();
        token1.value = focusNodes.get(0).getValue();
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = focusNodes.get(0).getBegin();
        token1.end = focusNodes.get(0).getEnd();
        tokens.add(token1);

        AnnotationToken token2 = new AnnotationToken();
        token2.value = focusNodes.get(1).getValue();
        token2.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token2.begin = focusNodes.get(1).getBegin();
        token2.end = focusNodes.get(1).getEnd();
        tokens.add(token2);

        tokens.addAll(ColValueOrDateColInst.tokens(param, formulas, amb));

        AnnotationToken token4 = new AnnotationToken();
        token4.value = focusNodes.get(3).getValue();
        token4.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token4.begin = focusNodes.get(3).getBegin();
        token4.end = focusNodes.get(3).getEnd();
        tokens.add(token4);

        tokens.add(NumberArg.token(integer));

        AnnotationToken token6 = new AnnotationToken();
        token6.value = focusNodes.get(5).getValue();
        token6.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token6.begin = focusNodes.get(5).getBegin();
        token6.end = focusNodes.get(5).getEnd();
        tokens.add(token6);

        return tokens;
    }

    // formula case
    public static JSONArray buildCase(JSONObject user) {
        String example = "add_days ( %s , %s )";
        example = String.format(example, "%s", SourcesUtils.decimalSug());
        JSONArray cases = new JSONArray();
        cases.addAll(FormulaCase.buildCaseDateCol(example, user));
        return cases;
    }
}
