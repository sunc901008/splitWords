package focus.search.instruction.functionInst.numberFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.FormulaCase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.BaseFunc;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.suggestions.SourcesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/27
 * description:
 */
//<moving-function> := <moving-average-function> |
//        <moving-max-function> |
//        <moving-min-function> |
//        <moving-sum-function>;
public class MovingFuncInstruction {

    // 完整指令 moving
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        return build1(focusPhrase.getFocusNodes().get(0).getChildren(), index, amb, formulas);
    }

    // 完整指令 <moving-average-function> := moving_average ( <number-source-column> , <integer> , <integer> , <attribute-list-params> );
    private static JSONArray build1(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
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

    // 其他指令一部分 <moving-average-function>
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        return arg1(focusPhrase.getFocusNodes().get(0).getChildren(), formulas);
    }

    // 其他指令一部分 <moving-average-function> := moving_average ( <number-source-column> , <integer> , <integer> , <attribute-list-params> );
    private static JSONObject arg1(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(2);
        FocusNode int1 = focusNodes.get(4);
        FocusNode int2 = focusNodes.get(6);
        FocusNode param2 = focusNodes.get(8);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();
        args.add(ColumnInstruction.arg(param1.getChildren()));
        args.add(NumberArg.arg(int1));
        args.add(NumberArg.arg(int2));
        args.addAll(BaseFunc.attributeListArgs(param2.getChildren()));
        expression.put("args", args);

        return expression;
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        return tokens1(focusPhrase.getFocusNodes().get(0).getChildren(), formulas, amb);
    }

    // annotation tokens
    private static List<AnnotationToken> tokens1(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<AnnotationToken> tokens = new ArrayList<>();
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(2);
        FocusNode int1 = focusNodes.get(4);
        FocusNode int2 = focusNodes.get(6);
        FocusNode param2 = focusNodes.get(8);
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

        tokens.add(AnnotationToken.singleCol(param1.getChildren(), amb));

        AnnotationToken token4 = new AnnotationToken();
        token4.value = focusNodes.get(3).getValue();
        token4.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token4.begin = focusNodes.get(3).getBegin();
        token4.end = focusNodes.get(3).getEnd();
        tokens.add(token4);

        tokens.add(NumberArg.token(int1));

        AnnotationToken token6 = new AnnotationToken();
        token6.value = focusNodes.get(5).getValue();
        token6.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token6.begin = focusNodes.get(5).getBegin();
        token6.end = focusNodes.get(5).getEnd();
        tokens.add(token6);

        tokens.add(NumberArg.token(int2));

        AnnotationToken token8 = new AnnotationToken();
        token8.value = focusNodes.get(7).getValue();
        token8.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token8.begin = focusNodes.get(7).getBegin();
        token8.end = focusNodes.get(7).getEnd();
        tokens.add(token8);

        tokens.addAll(BaseFunc.attributeListTokens(param2.getChildren(), amb));

        AnnotationToken token10 = new AnnotationToken();
        token10.value = focusNodes.get(9).getValue();
        token10.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token10.begin = focusNodes.get(9).getBegin();
        token10.end = focusNodes.get(9).getEnd();
        tokens.add(token10);

        return tokens;
    }

    // formula case
    public static JSONArray buildCase(JSONObject user, String key) {
        String example = key + " ( %s , %d , %d , %s )";
        JSONArray cases = new JSONArray();

        List<Column> numberColumns = SourcesUtils.colRandomSuggestions(user, Arrays.asList(Constant.DataType.INT, Constant.DataType.DOUBLE));
        List<Column> otherColumns = SourcesUtils.colRandomSuggestions(user, Arrays.asList(Constant.DataType.STRING, Constant.DataType.TIMESTAMP));
        if (numberColumns.size() > 0 && otherColumns.size() > 0) {
            String value1 = numberColumns.get(SourcesUtils.decimalSug(numberColumns.size())).getColumnDisplayName();
            int value2 = SourcesUtils.decimalSug(10);
            int value3 = SourcesUtils.decimalSug(10);
            String value4 = otherColumns.get(SourcesUtils.decimalSug(otherColumns.size())).getColumnDisplayName();
            cases.add(String.format(example, value1, value2, value3, value4));
        }

        cases.addAll(FormulaCase.buildCaseNumberCol(example, user));
        return cases;
    }
}
