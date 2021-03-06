package focus.search.instruction.filterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/9
 * description:
 */

//<all-string-column> = <column-value>
//<string-formula-column> = <column-value>
public class FilterStringColEqualInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();

        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        json1.put("expression", arg(focusPhrase, formulas));

        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);

        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        datas.addTokens(tokens(focusPhrase, formulas, amb));
        // annotation content
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(0);
        FocusNode param2 = focusNodes.get(2);
        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        JSONArray args = new JSONArray();
        expression.put("name", "equals");
        args.add(ColumnInstruction.arg(param1.getChildren(), formulas));
        args.addAll(ColumnValueInstruction.args(param2));
        expression.put("args", args);
        return expression;
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(0);
        FocusNode symbol = focusNodes.get(1);
        symbol = symbol.isHasChild() ? symbol.getChildren().getFirstNode() : symbol;
        FocusNode param2 = focusNodes.get(2);

        List<AnnotationToken> tokens = new ArrayList<>();

        tokens.add(AnnotationToken.singleCol(param1.getChildren(), amb, formulas));

        AnnotationToken token2 = new AnnotationToken();
        token2.value = symbol.getValue();
        token2.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token2.begin = symbol.getBegin();
        token2.end = symbol.getEnd();
        tokens.add(token2);

        tokens.addAll(ColumnValueInstruction.tokens(param2));
        return tokens;
    }

}
