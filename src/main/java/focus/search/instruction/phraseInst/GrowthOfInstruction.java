package focus.search.instruction.phraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/24
 * description:
 */
//<growth-of> := growth of <growth-of-measure> by <all-date-column> |
//              growth of <growth-of-measure> by <all-date-column> <year-over-year> |
//              growth of <growth-of-measure> by <all-date-column> <growth-of-by-date-interval> |
//              growth of <growth-of-measure> by <all-date-column> <growth-of-by-date-interval> <year-over-year>;
//
//<growth-of-measure> := <number-source-column> |
//        <growth-of-measure-operation> <number-source-column>;
//<growth-of-measure-operation> := sum |
//        average |
//        count |
//        max |
//        min |
//        standard deviation |
//        unique count |
//        variance;
//
//<growth-of-by-date-interval> := daily |
//        weekly |
//        monthly |
//        quarterly |
//        yearly;
//
//<year-over-year> := year over year;

public class GrowthOfInstruction {

//    {
//        "annotationId": [1],
//        "instId": "add_column_measure_for_growth",
//        "operation": "sum",
//        "column": 11
//    },
//    {
//        "annotationId": [1],
//        "instId": "use_column_for_growth_dimension",
//        "interval": "daily",
//        "period": "year-over-year",
//        "column": 6
//    }

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.GROWTH_OF_BY);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);

        AnnotationToken token1 = new AnnotationToken();
        token1.addToken("growth");
        token1.addToken("of");
        token1.value = "growth of";
        token1.type = Constant.AnnotationCategory.GROWTH_OF_BY;
        token1.begin = focusNodes.get(0).getBegin();
        token1.end = focusNodes.get(1).getEnd();
        datas.addToken(token1);

        FocusNode growthOfMeasure = focusNodes.get(2);// growth of
        FocusPhrase growthOf = growthOfMeasure.getChildren();
        if (growthOf.getFocusNodes().size() == 1) {
            json1.put("instId", Constant.InstIdType.GROWTH_COLUMN);
            Column column = growthOf.getLastNode().getColumn();
            json1.put("column", column.getColumnId());

            int begin = growthOf.getFirstNode().getBegin();
            int end = growthOf.getLastNode().getEnd();
            datas.addToken(AnnotationToken.singleCol(column, growthOf.size() == 2, begin, end, amb));
        } else {
            json1.put("instId", Constant.InstIdType.GROWTH_COLUMN_MEASURE);
            FocusPhrase growthOfMeasureOperation = growthOf.getFocusNodes().get(0).getChildren();
            AnnotationToken token2 = new AnnotationToken();
            StringBuilder operation = new StringBuilder();
            for (int i = 0; i < growthOfMeasureOperation.size(); i++) {
                if (operation.length() > 0) {
                    operation.append(" ");
                }
                String ope = growthOfMeasureOperation.getNodeNew(i).getValue().toLowerCase();
                operation.append(ope);
                token2.addToken(ope);
            }

            token2.value = operation.toString();
            token2.type = "numberKeyword";
            token2.begin = growthOfMeasureOperation.getFirstNode().getBegin();
            token2.end = growthOfMeasureOperation.getLastNode().getEnd();
            datas.addToken(token2);

            json1.put("operation", operation.toString());
            FocusPhrase tmp = growthOf.getFocusNodes().get(1).getChildren();
            Column column = tmp.getLastNode().getColumn();
            json1.put("column", column.getColumnId());

            int begin = tmp.getFirstNode().getBegin();
            int end = tmp.getLastNode().getEnd();
            datas.addToken(AnnotationToken.singleCol(column, tmp.size() == 2, begin, end, amb));
        }

        instructions.add(json1);

        AnnotationToken token4 = new AnnotationToken();
        token4.addToken("by");
        token4.value = "by";
        token4.type = Constant.AnnotationCategory.GROWTH_OF_BY;
        token4.begin = focusNodes.get(3).getBegin();
        token4.end = focusNodes.get(3).getEnd();
        datas.addToken(token4);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.GROWTH_DIMENSION);

        //<all-date-column>
        FocusPhrase datePhrase = focusNodes.get(4).getChildren();// by
        Column column = datePhrase.getLastNode().getColumn();
        json2.put("column", column.getColumnId());

        int begin = datePhrase.getFirstNode().getBegin();
        int end = datePhrase.getLastNode().getEnd();
        datas.addToken(AnnotationToken.singleCol(column, datePhrase.size() == 2, begin, end, amb));

        if (focusNodes.size() == 6) {
            FocusNode param3 = focusNodes.get(5);
            AnnotationToken token5 = new AnnotationToken();
            token5.begin = param3.getChildren().getFirstNode().getBegin();
            token5.end = param3.getChildren().getLastNode().getEnd();
            if ("<year-over-year>".equals(param3.getValue())) {
                json2.put("period", "year-over-year");
                token5.addToken("year");
                token5.addToken("over");
                token5.addToken("year");
                token5.value = "year over year";
                token5.type = "yearOverYear";
            } else {
                String interval = param3.getChildren().getFirstNode().getValue().toLowerCase();
                json2.put("interval", interval);
                token5.addToken(interval);
                token5.value = interval;
                token5.type = "growthOfByDateInterval";
            }
            datas.addToken(token5);
        } else if (focusNodes.size() == 7) {
            String interval = focusNodes.get(5).getChildren().getFirstNode().getValue().toLowerCase();
            json2.put("interval", interval);
            json2.put("period", "year-over-year");

            AnnotationToken token5 = new AnnotationToken();
            token5.begin = focusNodes.get(5).getChildren().getFirstNode().getBegin();
            token5.end = focusNodes.get(5).getChildren().getLastNode().getEnd();
            token5.addToken(interval);
            token5.value = interval;
            token5.type = "growthOfByDateInterval";
            datas.addToken(token5);

            AnnotationToken token6 = new AnnotationToken();
            token6.begin = focusNodes.get(6).getChildren().getFirstNode().getBegin();
            token6.end = focusNodes.get(6).getChildren().getLastNode().getEnd();
            token6.addToken("year");
            token6.addToken("over");
            token6.addToken("year");
            token6.value = "year over year";
            token6.type = "yearOverYear";
            datas.addToken(token6);
        }
        instructions.add(json2);

        JSONObject json3 = new JSONObject();
        json3.put("annotationId", annotationId);
        json3.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json3.put("content", datas);
        instructions.add(json3);

        return instructions;

    }

}
//{
//    "type": "phrase",
//    "id": 1,
//    "category": "growthOfBy",
//    "begin": 0,
//    "end": 69,
//    "tokens": [{
//    "tokens": ["growth",
//    "of"],
//    "type": "growthOfBy",
//    "value": "growth of",
//    "begin": 0,
//    "end": 9
//    },
//    {
//    "tokens": ["standard",
//    "deviation"],
//    "type": "numberKeyword",
//    "value": "standard deviation",
//    "begin": 9,
//    "end": 28
//    },
//    {
//    "description": "column <b>age<\\\/b> in <b>users<\\\/b>",
//    "tableName": "users",
//    "columnName": "age",
//    "columnId": 11,
//    "type": "measure",
//    "detailType": "floatMeasureColumn",
//    "tokens": ["age"],
//    "value": "age",
//    "begin": 28,
//    "end": 32
//    },
//    {
//    "tokens": ["by"],
//    "type": "growthOfBy",
//    "value": "by",
//    "begin": 32,
//    "end": 35
//    },
//    {
//    "description": "column <b>creationdate<\\\/b> in <b>users<\\\/b>",
//    "tableName": "users",
//    "columnName": "creationdate",
//    "columnId": 8,
//    "type": "attribute",
//    "detailType": "dateAttributeColumn",
//    "tokens": ["creationdate"],
//    "value": "creationdate",
//    "begin": 35,
//    "end": 48
//    },
//    {
//    "tokens": ["daily"],
//    "type": "growthOfByDateInterval",
//    "value": "daily",
//    "begin": 48,
//    "end": 54
//    },
//    {
//    "tokens": ["year"],
//    "type": null,
//    "value": "year",
//    "begin": 54,
//    "end": 59
//    },
//    {
//    "tokens": ["over"],
//    "type": null,
//    "value": "over",
//    "begin": 59,
//    "end": 64
//    },
//    {
//    "tokens": ["year"],
//    "type": null,
//    "value": "year",
//    "begin": 64,
//    "end": 69
//    }]
//}