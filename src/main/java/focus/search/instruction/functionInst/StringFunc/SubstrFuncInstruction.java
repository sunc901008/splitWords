package focus.search.instruction.functionInst.StringFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.FormulaCase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.ColValueOrStringColInst;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.suggestions.SourcesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
//<substr-function> := substr ( <string-columns> , <integer> , <integer> ) |
//        substr ( <column-value> , <integer> , <integer> );
public class SubstrFuncInstruction {
    // 完整指令 substr
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
        FocusNode param1 = focusPhrase.getFocusNodes().get(2);
        FocusNode param2 = focusPhrase.getFocusNodes().get(4);
        FocusNode param3 = focusPhrase.getFocusNodes().get(6);

        JSONObject arg = new JSONObject();
        arg.put("type", Constant.InstType.FUNCTION);
        arg.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        args.add(ColValueOrStringColInst.arg(param1, formulas));
        args.add(NumberArg.arg(param2));
        args.add(NumberArg.arg(param3));

        arg.put("args", args);
        return arg;
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<AnnotationToken> tokens = new ArrayList<>();
        AnnotationToken token1 = new AnnotationToken();
        token1.value = focusPhrase.getFocusNodes().get(0).getValue();
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = focusPhrase.getFocusNodes().get(0).getBegin();
        token1.end = focusPhrase.getFocusNodes().get(0).getEnd();
        tokens.add(token1);
//        substr ( <column-value> , <integer> , <integer> );
        AnnotationToken token2 = new AnnotationToken();
        token2.value = focusPhrase.getFocusNodes().get(1).getValue();
        token2.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token2.begin = focusPhrase.getFocusNodes().get(1).getBegin();
        token2.end = focusPhrase.getFocusNodes().get(1).getEnd();
        tokens.add(token2);

        tokens.addAll(ColValueOrStringColInst.tokens(focusPhrase.getFocusNodes().get(2), formulas, amb));

        AnnotationToken token4 = new AnnotationToken();
        token4.value = focusPhrase.getFocusNodes().get(3).getValue();
        token4.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token4.begin = focusPhrase.getFocusNodes().get(3).getBegin();
        token4.end = focusPhrase.getFocusNodes().get(3).getEnd();
        tokens.add(token4);

        tokens.add(NumberArg.token(focusPhrase.getFocusNodes().get(4)));

        AnnotationToken token6 = new AnnotationToken();
        token6.value = focusPhrase.getFocusNodes().get(5).getValue();
        token6.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token6.begin = focusPhrase.getFocusNodes().get(5).getBegin();
        token6.end = focusPhrase.getFocusNodes().get(5).getEnd();
        tokens.add(token6);

        tokens.add(NumberArg.token(focusPhrase.getFocusNodes().get(6)));

        AnnotationToken token8 = new AnnotationToken();
        token8.value = focusPhrase.getFocusNodes().get(7).getValue();
        token8.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token8.begin = focusPhrase.getFocusNodes().get(7).getBegin();
        token8.end = focusPhrase.getFocusNodes().get(7).getEnd();
        tokens.add(token8);
        return tokens;
    }

    // formula case
    public static JSONArray buildCase(JSONObject user) {
        String example = "substr ( %s )";

        JSONArray cases = new JSONArray();
        String value = SourcesUtils.stringSug();
        int start = SourcesUtils.decimalSug(value.length());
        int length = value.length() - SourcesUtils.decimalSug(value.length());
        if (length == 1) {
            start--;
        }
        example = String.format(example, "%s , " + String.format("%s , %s", start, length));
        cases.add(String.format(example, value));
        cases.addAll(FormulaCase.buildCaseStringCol(example, user));
        return cases;
    }

}
