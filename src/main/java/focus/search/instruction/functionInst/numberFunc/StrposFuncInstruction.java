package focus.search.instruction.functionInst.numberFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.FormulaCase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.ColValueOrStringColInst;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.meta.Column;
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
//<strpos-function> := strpos ( <single-column-value> , <single-column-value> ) |
//        strpos ( <all-string-column> , <single-column-value> );
public class StrposFuncInstruction {

    // 完整指令 strpos
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
        FocusNode param1 = focusNodes.get(2);
        FocusNode param2 = focusNodes.get(4);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        expression.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        args.add(ColValueOrStringColInst.arg(param1, formulas));
        args.add(ColumnValueInstruction.arg(param2));

        expression.put("args", args);
        return expression;
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        List<AnnotationToken> tokens = new ArrayList<>();
        FocusNode param1 = focusNodes.get(2);
        FocusNode param2 = focusNodes.get(4);
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

        tokens.addAll(ColValueOrStringColInst.tokens(param1, formulas, amb));

        AnnotationToken token4 = new AnnotationToken();
        token4.value = focusNodes.get(3).getValue();
        token4.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token4.begin = focusNodes.get(3).getBegin();
        token4.end = focusNodes.get(3).getEnd();
        tokens.add(token4);

        tokens.addAll(ColumnValueInstruction.tokens(param2));

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
        String example = "strpos ( %s , %s )";
        JSONArray cases = new JSONArray();

        List<Column> stringColumns = SourcesUtils.colRandomSuggestions(user, Constant.DataType.STRING);
        if (stringColumns.size() > 0) {
            String value1 = stringColumns.get(SourcesUtils.decimalSug(stringColumns.size())).getColumnDisplayName();
            String value2 = SourcesUtils.stringSug();
            cases.add(String.format(example, value1, value2));
        } else {
            cases.add(String.format(example, SourcesUtils.stringSug(), SourcesUtils.stringSug()));
        }

        cases.addAll(FormulaCase.buildCaseNumberCol(example, user));
        return cases;
    }
}
