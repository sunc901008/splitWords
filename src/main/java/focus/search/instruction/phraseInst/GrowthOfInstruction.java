package focus.search.instruction.phraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.meta.Column;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
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

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_column_measure_for_growth");

        FocusNode growthOfMeasure = focusNodes.get(2);// growth of
        FocusPhrase growthOf = growthOfMeasure.getChildren();
        if (growthOf.getFocusNodes().size() == 1) {
            Column column = growthOf.getNodeNew(0).getColumn();
            json1.put("column", column.getColumnId());
        } else {
            FocusPhrase growthOfMeasureOperation = growthOf.getFocusNodes().get(0).getChildren();
            StringBuilder operation = new StringBuilder();
            for (int i = 0; i < growthOfMeasureOperation.size(); i++) {
                if (operation.length() > 0) {
                    operation.append(" ");
                }
                operation.append(growthOfMeasureOperation.getNodeNew(i).getValue().toLowerCase());
            }
            json1.put("operation", operation);
            Column column = growthOf.getFocusNodes().get(1).getChildren().getNodeNew(0).getColumn();
            json1.put("column", column.getColumnId());
        }

        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "use_column_for_growth_dimension");

        FocusNode dateColumn = focusNodes.get(4);// by
        json2.put("column", dateColumn.getChildren().getLastNode().getColumn().getColumnId());
        if (focusNodes.size() == 6) {
            FocusNode param3 = focusNodes.get(5);
            if ("<year-over-year>".equals(param3.getValue())) {
                json2.put("period", "year-over-year");
            } else {
                json2.put("interval", param3.getChildren().getFirstNode().getValue().toLowerCase());
            }
        } else if (focusNodes.size() == 7) {
            json2.put("interval", focusNodes.get(5).getChildren().getFirstNode().getValue().toLowerCase());
            json2.put("period", "year-over-year");
        }
        instructions.add(json2);

        JSONObject json3 = new JSONObject();
        json3.put("annotationId", annotationId);
        json3.put("instId", "annotation");
        // annotation content
        json3.put("content", AnnotationBuild.build(focusPhrase, index, amb));
        instructions.add(json3);

        return instructions;

    }

}
