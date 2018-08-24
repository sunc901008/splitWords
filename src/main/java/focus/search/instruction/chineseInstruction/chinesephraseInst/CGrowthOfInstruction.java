package focus.search.instruction.chineseInstruction.chinesephraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Column;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/19
 * description:
 */
//<growth-of> := 按 <all-date-column> 计算的 <growth-of-measure> <growth-of-by-date-interval> |
//        按 <all-date-column> 计算的 <growth-of-measure> <growth-of-by-date-interval> <year-over-year>|
//        <year-over-year> 按 <all-date-column> 计算的 <growth-of-measure> <growth-of-by-date-interval>;
public class CGrowthOfInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb) {
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, Constant.AnnotationCategory.GROWTH_OF_BY);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);

        boolean hasYearOverYear = false;
        int flag = 0;
        FocusNode keyword1 = focusNodes.get(flag++);//按
        if ("<year-over-year>".equals(keyword1.getValue())) {//<year-over-year>
            AnnotationToken token1 = new AnnotationToken();
            FocusNode yearOverYear = keyword1.getChildren().getFirstNode();
            token1.addToken(yearOverYear.getValue());
            token1.value = yearOverYear.getValue();
            token1.type = "yearOverYear";
            token1.begin = yearOverYear.getBegin();
            token1.end = yearOverYear.getEnd();
            datas.addToken(token1);
            keyword1 = focusNodes.get(flag++);
            hasYearOverYear = true;
        }

        AnnotationToken token2 = new AnnotationToken();
        token2.addToken("按");
        token2.value = "按";
        token2.type = Constant.AnnotationCategory.GROWTH_OF_BY;
        token2.begin = keyword1.getBegin();
        token2.end = keyword1.getEnd();
        datas.addToken(token2);

        //<all-date-column>
        FocusPhrase datePhrase = focusNodes.get(flag++).getChildren();
        Column dateCol = datePhrase.getLastNode().getColumn();

        int dateBegin = datePhrase.getFirstNode().getBegin();
        int dateEnd = datePhrase.getLastNode().getEnd();
        datas.addToken(AnnotationToken.singleCol(dateCol, datePhrase.size() == 2, dateBegin, dateEnd, amb));

        FocusNode keyword2 = focusNodes.get(flag++);//计算的
        AnnotationToken token4 = new AnnotationToken();
        token4.addToken("计算的");
        token4.value = "计算的";
        token4.type = Constant.AnnotationCategory.GROWTH_OF_BY;
        token4.begin = keyword2.getBegin();
        token4.end = keyword2.getEnd();
        datas.addToken(token4);

        FocusNode growthOfMeasure = focusNodes.get(flag++);
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

            FocusPhrase tmp = growthOf.getFocusNodes().get(0).getChildren();
            Column column = tmp.getLastNode().getColumn();
            json1.put("column", column.getColumnId());

            int begin = tmp.getFirstNode().getBegin();
            int end = tmp.getLastNode().getEnd();
            datas.addToken(AnnotationToken.singleCol(column, tmp.size() == 2, begin, end, amb));

            FocusPhrase growthOfMeasureOperation = growthOf.getFocusNodes().get(1).getChildren();
            AnnotationToken token5 = new AnnotationToken();
            FocusNode operate = growthOfMeasureOperation.getFirstNode();
            token5.addToken(operate.getValue());
            token5.value = operate.getValue();
            token5.type = "numberKeyword";
            token5.begin = operate.getBegin();
            token5.end = operate.getEnd();
            datas.addToken(token5);

            json1.put("operation", getOperation(growthOfMeasureOperation));
        }

        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.GROWTH_DIMENSION);
        json2.put("column", dateCol.getColumnId());

        //<growth-of-by-date-interval>
        FocusPhrase dateInterval = focusNodes.get(flag++).getChildren();
        AnnotationToken token6 = new AnnotationToken();
        FocusNode interval = dateInterval.getFirstNode();
        token6.begin = interval.getBegin();
        token6.end = interval.getEnd();
        json2.put("interval", getInterval(dateInterval));
        token6.addToken(interval.getValue());
        token6.value = interval.getValue();
        token6.type = "growthOfByDateInterval";
        datas.addToken(token6);

        if (focusNodes.size() - 1 > flag) {//<year-over-year>
            AnnotationToken token = new AnnotationToken();
            FocusNode yearOverYear = focusNodes.get(flag).getChildren().getFirstNode();
            token.addToken(yearOverYear.getValue());
            token.value = yearOverYear.getValue();
            token.type = "yearOverYear";
            token.begin = yearOverYear.getBegin();
            token.end = yearOverYear.getEnd();
            datas.addToken(token);
            hasYearOverYear = true;
        }

        if (hasYearOverYear) {
            json2.put("period", "year-over-year");
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

    //    <growth-of-measure-sum-operation> := 的总和;
    //    <growth-of-measure-average-operation> := 的平均值;
    //    <growth-of-measure-count-operation> := 的数量;
    //    <growth-of-measure-max-operation> := 的最大值;
    //    <growth-of-measure-min-operation> := 的最小值;
    //    <growth-of-measure-standard-deviation-operation> := 的标准差;
    //    <growth-of-measure-unique-count-operation> := 去重后的数量;
    //    <growth-of-measure-variance-operation> := 的方差;
    private static String getOperation(FocusPhrase growthOfMeasureOperation) {
        String instName = growthOfMeasureOperation.getInstName();
        switch (instName) {
            case "<growth-of-measure-sum-operation>":
                return "sum";
            case "<growth-of-measure-average-operation>":
                return "average";
            case "<growth-of-measure-count-operation>":
                return "count";
            case "<growth-of-measure-max-operation>":
                return "max";
            case "<growth-of-measure-min-operation>":
                return "min";
            case "<growth-of-measure-standard-deviation-operation>":
                return "standard deviation";
            case "<growth-of-measure-unique-count-operation>":
                return "unique count";
            case "<growth-of-measure-variance-operation>":
                return "variance";
            default:
                return "sum";
        }
    }

    //    <growth-of-daily-interval> := 的日增长率;
    //    <growth-of-weekly-interval> := 的周增长率;
    //    <growth-of-monthly-interval> := 的增长率 |
    //        的月增长率;
    //    <growth-of-quarterly-interval> := 的季度增长率;
    //    <growth-of-yearly-interval> := 的年增长率;
    private static String getInterval(FocusPhrase dateInterval) {
        String instName = dateInterval.getInstName();
        switch (instName) {
            case "<growth-of-daily-interval>":
                return "daily";
            case "<growth-of-weekly-interval>":
                return "weekly";
            case "<growth-of-monthly-interval>":
                return "monthly";
            case "<growth-of-quarterly-interval>":
                return "quarterly";
            case "<growth-of-yearly-interval>":
                return "yearly";
            default:
                return "monthly";
        }
    }

}
