package focus.search.instruction.chineseInstruction.chinesephraseInst;

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
 * date: 2018/5/28
 * description:
 */
//<sort-by> := 按 <all-columns> <sort-by-ascending> |
//        按 <all-columns> <sort-by-descending> |
//        按 <all-columns> <sort-by-chinese>;
//<sort-by-chinese> := 排序 |
//        排序的;
//<sort-by-ascending> := 升序 |
//        升序的;
//<sort-by-descending> := 降序 |
//        降序的;
public class CSortByInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.SORT_BY_ORDER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.SORT_BY);

        FocusNode first = focusNodes.get(0);
        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(first.getValue());
        token1.value = first.getValue();
        token1.type = Constant.AnnotationCategory.SORT_BY_ORDER;
        token1.begin = first.getBegin();
        token1.end = first.getEnd();
        datas.addToken(token1);

        FocusNode param = focusNodes.get(1);
        json1.put("name", Base.InstName(param.getChildren()));

        JSONObject json = AllColumnsInstruction.build(param.getChildren(), formulas);
        String type = json.getString("type");
        JSONObject arg = new JSONObject();
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            arg.put("type", "column");
            Column column = (Column) json.get("column");
            arg.put("value", column.getColumnId());
            int begin = param.getChildren().getFirstNode().getBegin();
            int end = param.getChildren().getLastNode().getEnd();
            datas.addToken(AnnotationToken.singleCol(column, Constant.InstType.TABLE_COLUMN.equals(type), begin, end, amb));
        } else if (Constant.InstType.FUNCTION.equals(type)) {
            arg = json.getJSONObject(Constant.InstType.FUNCTION);
        }

        json1.put("expression", arg);
        FocusNode sortOrder = focusNodes.get(2);
        FocusNode order = sortOrder.getChildren().getFirstNode();
        AnnotationToken token3 = new AnnotationToken();
        token3.addToken(order.getValue());
        token3.value = order.getValue();
        token3.type = Constant.AnnotationCategory.SORT_BY_ORDER;
        token3.begin = focusNodes.get(3).getBegin();
        token3.end = focusNodes.get(3).getEnd();
        datas.addToken(token3);
        if ("<sort-by-ascending>".equals(sortOrder.getValue())) {
            json1.put("sortOrder", "ascending");
        } else if ("<sort-by-descending>".equals(sortOrder.getValue())) {
            json1.put("sortOrder", "descending");
        }

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
