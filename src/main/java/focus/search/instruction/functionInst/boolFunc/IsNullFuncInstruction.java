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
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.instruction.sourceInst.AllColumnsInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
//<isnull-function> := isnull ( <all-columns> ) |
//        isnull ( <number> );
public class IsNullFuncInstruction {
    private static final String example = "isnull ( %s )";

    // 完整指令 isnull
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.EXPRESSION);
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

        // annotation content
        datas.addTokens(tokens(focusPhrase, formulas, amb));
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode param = focusPhrase.getFocusNodes().get(2);

        JSONObject arg = new JSONObject();
        arg.put("type", Constant.InstType.FUNCTION);
        arg.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        if ("<all-columns>".equals(param.getValue())) {
            JSONObject json = AllColumnsInstruction.build(param.getChildren(), formulas);
            String type = json.getString("type");
            JSONObject arg1 = new JSONObject();
            if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
                arg1.put("type", "column");
                arg1.put("value", ((Column) json.get("column")).getColumnId());
            } else if (Constant.InstType.FUNCTION.equals(type)) {
                arg1 = json.getJSONObject(Constant.InstType.FUNCTION);
            }
            args.add(arg1);
        } else if ("<number>".equals(param.getValue())) {
            args.add(NumberArg.arg(param));
        }
        arg.put("args", args);
        return arg;
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<AnnotationToken> tokens = new ArrayList<>();
        FocusNode param = focusPhrase.getFocusNodes().get(2);
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

        if ("<all-columns>".equals(param.getValue())) {
            tokens.addAll(AllColumnsInstruction.tokens(param.getChildren(), formulas, amb));
        } else if ("<number>".equals(param.getValue())) {
            tokens.add(NumberArg.token(param));
        }

        AnnotationToken token4 = new AnnotationToken();
        token4.value = focusPhrase.getFocusNodes().get(3).getValue();
        token4.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token4.begin = focusPhrase.getFocusNodes().get(3).getBegin();
        token4.end = focusPhrase.getFocusNodes().get(3).getEnd();
        tokens.add(token4);

        return tokens;
    }

    // formula case
    public static JSONArray buildCase(JSONObject user) {
        JSONArray cases = new JSONArray();
        cases.addAll(FormulaCase.buildCaseNumber(example));
        cases.addAll(FormulaCase.buildCaseAllCol(example, user));
        return cases;
    }
}
