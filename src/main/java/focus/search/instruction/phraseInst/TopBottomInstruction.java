package focus.search.instruction.phraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.Base;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.NumberColInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<top-n> := top <integer> <number-columns> |
//        top <number-columns>;
//
//<bottom-n> := bottom <integer> <number-columns> |
//        bottom <number-columns>;
public class TopBottomInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        int flag = 0;
        int n = 1;
        FocusNode keyword = focusPhrase.getFocusNodes().get(flag++);
        json1.put("instId", Constant.InstIdType.TOP_BOTTOM);
        json1.put("type", keyword.getValue());
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, keyword.getValue());

        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(keyword.getValue());
        token1.value = keyword.getValue();
        token1.type = keyword.getValue();
        token1.begin = keyword.getBegin();
        token1.end = keyword.getEnd();
        datas.addToken(token1);

        FocusNode integer = focusPhrase.getFocusNodes().get(flag++);
        FocusNode param;
        if ("<integer>".equals(integer.getValue())) {
            FocusNode intBaseNode = integer.getChildren().getNodeNew(0);
            n = Integer.parseInt(intBaseNode.getValue());
            param = focusPhrase.getFocusNodes().get(flag);
            AnnotationToken token2 = new AnnotationToken();
            token2.value = n;
            token2.type = Constant.FNDType.INTEGER;
            token2.begin = intBaseNode.getBegin();
            token2.end = intBaseNode.getEnd();
            datas.addToken(token2);
        } else {
            param = integer;
        }
        json1.put("n", n);
        json1.put("name", Base.InstName(param.getChildren()));

        datas.addTokens(NumberColInstruction.tokens(param.getChildren(), formulas, amb));

        json1.put("expression", NumberColInstruction.arg(param.getChildren(), formulas));
        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;

    }

}
