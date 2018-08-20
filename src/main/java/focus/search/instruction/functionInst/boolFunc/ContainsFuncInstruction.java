package focus.search.instruction.functionInst.boolFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.Base;
import focus.search.controller.common.FormulaCase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.ColValueOrStringColInst;
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
//<contains-function> := contains ( <string-columns> , <string-columns> ) |
//        contains ( <column-value> , <string-columns> ) |
//        contains ( <string-columns> , <column-value> ) |
//        contains ( <column-value> , <column-value> );
public class ContainsFuncInstruction {

    // 完整指令 contains
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.EXPRESSION);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        json1.put("expression", arg(focusPhrase, formulas));
        json1.put("name", Base.InstName(focusPhrase));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);

        datas.addTokens(tokens(focusPhrase, formulas, amb));
        // annotation content
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(2);
        FocusNode param2 = focusPhrase.getFocusNodes().get(4);

        JSONObject arg = new JSONObject();
        arg.put("type", Constant.InstType.FUNCTION);
        arg.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        args.add(ColValueOrStringColInst.arg(param1, formulas));
        args.add(ColValueOrStringColInst.arg(param2, formulas));

        arg.put("args", args);
        return arg;
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<AnnotationToken> tokens = new ArrayList<>();
        AnnotationToken token1 = new AnnotationToken();
        token1.value = focusPhrase.getFocusNodes().get(0).getValue();
        token1.type = Constant.AnnotationTokenType.SYMBOL;
        token1.begin = focusPhrase.getFocusNodes().get(0).getBegin();
        token1.end = focusPhrase.getFocusNodes().get(0).getEnd();
        tokens.add(token1);

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

        tokens.addAll(ColValueOrStringColInst.tokens(focusPhrase.getFocusNodes().get(4), formulas, amb));

        AnnotationToken token6 = new AnnotationToken();
        token6.value = focusPhrase.getFocusNodes().get(5).getValue();
        token6.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token6.begin = focusPhrase.getFocusNodes().get(5).getBegin();
        token6.end = focusPhrase.getFocusNodes().get(5).getEnd();
        tokens.add(token6);
        return tokens;
    }

    // formula case
    public static JSONArray buildCase(JSONObject user) {
        String example = "contains ( %s )";
        example = String.format(example, "%s , " + SourcesUtils.stringSug());
        JSONArray cases = new JSONArray();
        cases.add(String.format(example, SourcesUtils.stringSug() + " , " + SourcesUtils.stringSug()));
        cases.addAll(FormulaCase.buildCaseStringCol(example, user));
        return cases;
    }
}
//{
//    "type": "phrase",
//    "id": 1,
//    "category": "expression",
//    "begin": 0,
//    "end": 34,
//    "tokens": [{
//    "type": "symbol",
//    "value": "contains",
//    "begin": 0,
//    "end": 8
//    },
//    {
//    "type": "punctuationMark",
//    "value": "(",
//    "begin": 8,
//    "end": 10
//    },
//    {
//    "description": "column <b>displayname<\\\/b> in <b>users<\\\/b>",
//    "tableName": "users",
//    "columnName": "displayname",
//    "columnId": 10,
//    "type": "attribute",
//    "detailType": "stringAttributeColumn",
//    "tokens": ["displayname"],
//    "value": "displayname",
//    "begin": 10,
//    "end": 22
//    },
//    {
//    "type": "punctuationMark",
//    "value": ",",
//    "begin": 22,
//    "end": 25
//    },
//    {
//    "type": "wholeString",
//    "value": "a",
//    "begin": 25,
//    "end": 32
//    },
//    {
//    "isExpressionEnd": true,
//    "type": "punctuationMark",
//    "value": ")",
//    "begin": 32,
//    "end": 34
//    }]
//}