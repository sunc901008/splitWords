package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.Base;
import focus.search.controller.common.FormulaAnalysis;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.DateFuncInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */

//<date-columns> := <all-date-column> |
//        <date-function-column> |
//        ( <date-function-column> );
public class DateColInstruction {

    // 完整指令 columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        if (Constant.FNDType.FORMULA.equals(focusPhrase.getLastNode().getType())) {
            return FormulaColumnInstruction.build(focusPhrase, index, formulas);
        }
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_EXPRESSION);
        json1.put("category", Constant.AnnotationCategory.EXPRESSION);
        json1.put("type", Constant.ColumnType.ATTRIBUTE);
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE);

        json1.put("name", Base.InstName(focusPhrase));
//        json1.put("aggregation", aggregation);

        datas.addTokens(tokens(focusPhrase, formulas, amb));

        json1.put("expression", arg(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);

        // annotation content
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        switch (node.getValue()) {
            case "<all-date-column>":
                return ColumnInstruction.arg(node.getChildren(), formulas);
            case "<date-function-column>":
                return DateFuncInstruction.arg(node.getChildren(), formulas);
            case FormulaAnalysis.LEFT_BRACKET:
                return DateFuncInstruction.arg(focusPhrase.getFocusNodes().get(1).getChildren(), formulas);
//            case "<date-formula-column>":
//                return FormulaColumnInstruction.arg(node.getChildren(), formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        List<AnnotationToken> tokens = new ArrayList<>();
        switch (node.getValue()) {
            case "<all-date-column>":
                tokens.add(AnnotationToken.singleCol(node.getChildren(), amb, formulas));
                return tokens;
            case FormulaAnalysis.LEFT_BRACKET:
                AnnotationToken token1 = new AnnotationToken();
                token1.value = node.getValue();
                token1.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
                token1.begin = node.getBegin();
                token1.end = node.getEnd();
                tokens.add(token1);

                tokens.addAll(DateFuncInstruction.tokens(focusPhrase.getFocusNodes().get(1).getChildren(), formulas, amb));

                AnnotationToken token2 = new AnnotationToken();
                token2.value = focusPhrase.getFocusNodes().get(2).getValue();
                token2.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
                token2.begin = focusPhrase.getFocusNodes().get(2).getBegin();
                token2.end = focusPhrase.getFocusNodes().get(2).getEnd();
                tokens.add(token2);

                return tokens;
            case "<date-function-column>":
                return DateFuncInstruction.tokens(node.getChildren(), formulas, amb);
//            case "<date-formula-column>":
//                return FormulaColumnInstruction.tokens(node.getChildren(), formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
