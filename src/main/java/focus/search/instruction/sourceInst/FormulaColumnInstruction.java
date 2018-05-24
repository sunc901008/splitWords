package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/26
 * description:
 */
public class FormulaColumnInstruction {

    // 完整指令 formula-column
    public static JSONArray build(FocusPhrase focusPhrase, int index, List<Formula> formulas) throws FocusInstructionException {
        FocusNode formulaNode = focusPhrase.getFirstNode();

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_EXPRESSION);
        json1.put("name", formulaNode.getValue());
        json1.put("category", Constant.AnnotationCategory.EXPRESSION_OR_LOGICAL);
        Formula formula = getFormula(formulas, formulaNode.getValue());
        json1.put("aggregation", formula.getAggregation());
        json1.put("type", formula.getColumnType());
        json1.put("expression", JSONObject.parse(formula.getInstruction().toJSONString()));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);

        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.FORMULA_NAME);

        datas.addToken(AnnotationToken.singleFormula(formulaNode, formula));

        // annotation content
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    private static Formula getFormula(List<Formula> formulas, String formulaName) throws FocusInstructionException {
        for (Formula f : formulas) {
            if (f.getName().equalsIgnoreCase(formulaName)) {
                return f;
            }
        }
        JSONObject exception = new JSONObject();
        exception.put("formulas", formulas);
        exception.put("formulaName", formulaName);
        throw new FocusInstructionException(exception);
    }

}
