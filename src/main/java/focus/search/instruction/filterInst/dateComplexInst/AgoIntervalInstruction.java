package focus.search.instruction.filterInst.dateComplexInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.AmbiguityDatas;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/22
 * description:
 */
//<ago-filter> := <ago-days-filter> |
//        <ago-weeks-filter> |
//        <ago-months-filter> |
//        <ago-quarters-filter> |
//        <ago-years-filter> |
//        <ago-minutes-filter> |
//        <ago-hours-filter>;
public class AgoIntervalInstruction {
    private static final Logger logger = Logger.getLogger(AgoIntervalInstruction.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("AgoIntervalInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode fn = focusNodes.get(0);

        String key;
        switch (fn.getValue()) {
            case "<ago-days-filter>":
                key = "day";
                break;
            case "<ago-weeks-filter>":
                key = "week";
                break;
            case "<ago-months-filter>":
                key = "month";
                break;
            case "<ago-quarters-filter>":
                key = "quarter";
                break;
            case "<ago-years-filter>":
                key = "year";
                break;
            case "<ago-minutes-filter>":
                key = "minute";
                break;
            case "<ago-hours-filter>":
                key = "hour";
                break;
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
        return build(fn.getChildren(), index, amb, dateColumns, key);
    }

    //<ago-days-filter> := <integer> days ago;
    //<ago-weeks-filter> := <integer> weeks ago;
    //<ago-months-filter> := <integer> months ago;
    //<ago-quarters-filter> := <integer> quarters ago;
    //<ago-years-filter> := <integer> years ago;
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Column> dateColumns, String key) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        FocusNode param = focusPhrase.getFirstNode();
        int integer = Integer.parseInt(param.getValue());
        FocusNode keyword = focusPhrase.getNodeNew(2);
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.FILTER, Constant.AnnotationCategory.FILTER);
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", Constant.InstIdType.ADD_LOGICAL_FILTER);

        JSONObject json = CommonFunc.checkAmb(focusPhrase, keyword, dateColumns, amb, "ago");
        AmbiguityDatas ambiguity = (AmbiguityDatas) json.get("ambiguity");
        Column dateCol = (Column) json.get("column");

        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(param.getValue());
        token1.value = param.getValue();
        token1.begin = param.getBegin();
        token1.end = param.getEnd();
        token1.type = param.getType();
        datas.addToken(token1);

        FocusNode node = focusPhrase.getNodeNew(1);
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(node.getValue());
        token2.value = node.getValue();
        token2.begin = node.getBegin();
        token2.end = node.getEnd();
        token2.type = node.getType();
        datas.addToken(token2);

        AnnotationToken token3 = new AnnotationToken();
        token3.description = "column " + dateCol.getColumnDisplayName() + " in " + dateCol.getSourceName();
        token3.tableName = dateCol.getSourceName();
        token3.columnName = dateCol.getColumnDisplayName();
        token3.columnId = dateCol.getColumnId();
        token3.addToken(keyword.getValue());
        token3.value = keyword.getValue();
        token3.type = Constant.AnnotationCategory.ATTRIBUTE_COLUMN;
        token3.begin = keyword.getBegin();
        token3.end = keyword.getEnd();
        token3.ambiguity = ambiguity;
        datas.addToken(token3);

        JSONObject expression = new JSONObject();
        expression.put("name", "<");
        expression.put("type", "function");
        JSONArray argEnds = new JSONArray();
        JSONObject argEnd1 = new JSONObject();
        argEnd1.put("type", Constant.InstType.COLUMN);
        argEnd1.put("value", dateCol.getColumnId());
        argEnds.add(argEnd1);
        JSONObject argEnd2 = new JSONObject();
        argEnd2.put("type", Constant.InstType.DATE);
        argEnd2.put("value", CommonFunc.agoParams(key, integer));
        argEnds.add(argEnd2);
        expression.put("args", argEnds);

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
