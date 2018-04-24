package focus.search.instruction.phraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.annotations.AnnotationBuild;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.sourceInst.AllColumnsInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;

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

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression_for_sort");

        datas.id = index;
        datas.begin = focusPhrase.getFirstNode().getBegin();
        datas.end = focusPhrase.getLastNode().getEnd();
        datas.type = Constant.AnnotationType.PHRASE;
        datas.category = Constant.AnnotationCategory.SORT_BY_ORDER;

        AnnotationToken token1 = new AnnotationToken();
        token1.tokens.add("sort");
        token1.tokens.add("by");
        token1.value = "sort by";
        token1.type = Constant.AnnotationCategory.SORT_BY_ORDER;
        token1.begin = focusNodes.get(0).getBegin();
        token1.end = focusNodes.get(1).getEnd();
        datas.tokens.add(token1);

        FocusNode param = focusPhrase.getFocusNodes().get(2);

        JSONObject json = AllColumnsInstruction.build(param.getChildren(), formulas);
        String type = json.getString("type");
        JSONObject arg = new JSONObject();
        // todo
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            arg.put("type", "column");
            Column column = (Column) json.get("column");
            arg.put("value", column.getColumnId());
            int begin = param.getChildren().getFirstNode().getBegin();
            int end = param.getChildren().getLastNode().getEnd();
            datas.tokens.add(AnnotationToken.singleCol(column, Constant.InstType.TABLE_COLUMN.equals(type), begin, end));
        } else if (Constant.InstType.FUNCTION.equals(type)) {
            arg = json.getJSONObject(Constant.InstType.FUNCTION);
        }

        json1.put("expression", arg);
        String sortOrder = null;
        if (focusNodes.size() == 4) {
            sortOrder = focusNodes.get(3).getValue();
            AnnotationToken token3 = new AnnotationToken();
            token3.value = sortOrder;
            token3.type = "sortOrder";
            token3.begin = focusNodes.get(3).getBegin();
            token3.end = focusNodes.get(3).getEnd();
            token3.tokens.add(sortOrder);
            datas.tokens.add(token3);
        }
        json1.put("sortOrder", sortOrder);

        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");
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
