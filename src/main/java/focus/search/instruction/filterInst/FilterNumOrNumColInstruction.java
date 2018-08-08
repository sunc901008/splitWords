package focus.search.instruction.filterInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.NumberOrNumColInst;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */

//<number-columns> <bool-symbol> <number>
//<number-columns> <bool-symbol> <number-columns>
//<number> <bool-symbol> <number-columns>
//<number> <bool-symbol> <number>

public class FilterNumOrNumColInstruction {
    private static final Logger logger = Logger.getLogger(FilterNumOrNumColInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        logger.info("instruction arg. focusPhrase:" + focusPhrase.toJSON());
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode param1 = focusNodes.get(0);
        FocusNode symbol = focusNodes.get(1).getChildren().getFirstNode();
        FocusNode param2 = focusNodes.get(2);

        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);

        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        JSONObject expression = new JSONObject();
        expression.put("type", Constant.InstType.FUNCTION);
        JSONArray args = new JSONArray();

        String symbolValue = Constant.SymbolMapper.symbol.get(symbol.getValue());
        expression.put("name", symbolValue != null ? symbolValue : symbol.getValue());

        args.add(NumberOrNumColInst.arg(param1, formulas));

        datas.addTokens(NumberOrNumColInst.tokens(param1, formulas, amb));

        AnnotationToken token2 = new AnnotationToken();
        token2.value = symbol.getValue();
        token2.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
        token2.begin = symbol.getBegin();
        token2.end = symbol.getEnd();
        datas.addToken(token2);

        args.add(NumberOrNumColInst.arg(param2, formulas));

        datas.addTokens(NumberOrNumColInst.tokens(param2, formulas, amb));

        expression.put("args", args);
        json1.put("expression", expression);

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
