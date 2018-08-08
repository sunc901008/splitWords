package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.Base;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<bool-columns> := <all-bool-column>;
public class BoolColInstruction {

    // 完整指令 bool-columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
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
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.ATTRIBUTE_COLUMN);

        json1.put("type", Constant.ColumnType.ATTRIBUTE);
        json1.put("name", Base.InstName(focusPhrase));
        json1.put("expression", arg(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);

        datas.addToken(AnnotationToken.singleCol(focusPhrase, amb, formulas));

        // annotation content
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        if ("<all-bool-column>".equals(node.getValue())) {
            return ColumnInstruction.arg(node.getChildren(), formulas);
        } else {
            return FormulaColumnInstruction.arg(node.getChildren(), formulas);
        }
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusNode focusNode, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusPhrase focusPhrase = focusNode.getChildren();
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        if ("<all-bool-column>".equals(node.getValue())) {
            return tokensColumn(focusNode, amb);
        } else {
            return FormulaColumnInstruction.tokens(focusPhrase, formulas);
        }
    }

    private static List<AnnotationToken> tokensColumn(FocusNode focusNode, JSONObject amb) {
        List<AnnotationToken> tokens = new ArrayList<>();
        FocusPhrase fp = focusNode.getChildren();
        int begin = fp.getFirstNode().getBegin();
        int end = fp.getLastNode().getEnd();
        tokens.add(AnnotationToken.singleCol(fp.getLastNode().getColumn(), fp.size() == 2, begin, end, amb));
        return tokens;
    }

}
