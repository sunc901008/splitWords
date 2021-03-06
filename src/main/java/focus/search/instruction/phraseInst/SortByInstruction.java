package focus.search.instruction.phraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.Base;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.AllColumnsInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<sort-by> := sort by <all-columns> |
//        sort by <all-columns> desc |
//        sort by <all-columns> asc;
public class SortByInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.SORT_BY_ORDER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.SORT_BY);

        AnnotationToken token1 = new AnnotationToken();
        token1.addToken("sort");
        token1.addToken("by");
        token1.value = "sort by";
        token1.type = Constant.AnnotationCategory.SORT_BY_ORDER;
        token1.begin = focusNodes.get(0).getBegin();
        token1.end = focusNodes.get(1).getEnd();
        datas.addToken(token1);

        FocusNode param = focusPhrase.getFocusNodes().get(2);
        json1.put("name", Base.InstName(param.getChildren()));

        datas.addTokens(AllColumnsInstruction.tokens(param.getChildren(), formulas, amb));

        json1.put("expression", AllColumnsInstruction.arg(param.getChildren(), formulas));
        String sortOrder = null;
        if (focusNodes.size() == 4) {
            sortOrder = focusNodes.get(3).getValue();
            AnnotationToken token3 = new AnnotationToken();
            token3.value = sortOrder;
            token3.type = "sortOrder";
            token3.begin = focusNodes.get(3).getBegin();
            token3.end = focusNodes.get(3).getEnd();
            token3.addToken(sortOrder);
            datas.addToken(token3);
        }
        json1.put("sortOrder", sortOrder);

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

//{
//    "type": "phrase",
//    "id": 1,
//    "category": "sortByOrder",
//    "begin": 0,
//    "end": 13,
//    "tokens": [{
//    "tokens": ["sort",
//    "by"],
//    "type": "sortByOrder",
//    "value": "sort by",
//    "begin": 0,
//    "end": 7
//    },
//    {
//    "description": "column <b>views<\\\/b> in <b>users<\\\/b>",
//    "tableName": "users",
//    "columnName": "views",
//    "columnId": 7,
//    "type": "measure",
//    "detailType": "floatMeasureColumn",
//    "tokens": ["views"],
//    "value": "views",
//    "begin": 7,
//    "end": 13
//    }]
//}
