package focus.search.instruction.chineseInstruction.chinesephraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.instruction.sourceInst.NumberColInstruction;
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
//<top-n-chinese> := 前 |
//        排前 |
//        排名前;
//<top-bottom-n-measure-word-chinese> := 的 |
//        名的;
//<top-n> := <top-n-chinese> <integer> <top-bottom-n-measure-word-chinese> <number-columns> |
//        <number-columns> <top-n-chinese> <integer> <top-bottom-n-measure-word-chinese> |
//        <top-1>;
//<top-1-chinese> := 排名最前的 |
//        排最前的 |
//        最前的;
//<top-1> := <top-1-chinese> <number-columns> |
//        <number-columns> <top-1-chinese>;
public class CTopBottomInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        String key = "<top-n>".equals(focusPhrase.getInstName()) ? "top" : "bottom";
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        if (focusPhrase.size() == 1) {
            return build1(fn.getChildren(), index, amb, formulas, key);
        }
        if ("<number-columns>".equals(fn.getValue())) {
            return build2(focusPhrase, index, amb, formulas, key);
        }
        return build3(focusPhrase, index, amb, formulas, key);
    }

    //<top-1-chinese> := 排名最前的 |
    //        排最前的 |
    //        最前的;
    //<top-1> := <top-1-chinese> <number-columns> |
    //        <number-columns> <top-1-chinese>;
    private static JSONArray build1(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, String key) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", String.format("set_%s_n", key));

        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, key);
        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        FocusNode first = focusNodes.get(0);
        JSONObject expression = new JSONObject();
        if ("<number-columns>".equals(first.getValue())) {//<number-columns> <top-1-chinese>
            JSONObject json = NumberColInstruction.build(first.getChildren(), formulas);
            String type = json.getString("type");
            if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
                expression.put("type", "column");
                Column column = (Column) json.get("column");
                expression.put("value", column.getColumnId());
                int begin = first.getChildren().getFirstNode().getBegin();
                int end = first.getChildren().getLastNode().getEnd();
                datas.addToken(AnnotationToken.singleCol(column, Constant.InstType.TABLE_COLUMN.equals(type), begin, end, amb));
            } else if (Constant.InstType.FUNCTION.equals(type)) {
                expression = json.getJSONObject(Constant.InstType.FUNCTION);
            }
            FocusNode keyword = focusNodes.get(1).getChildren().getFirstNode();
            AnnotationToken token1 = new AnnotationToken();
            token1.addToken(keyword.getValue());
            token1.value = keyword.getValue();
            token1.type = key + "N";
            token1.begin = keyword.getBegin();
            token1.end = keyword.getEnd();
            datas.addToken(token1);
        } else {//<top-1-chinese> <number-columns>
            FocusNode keyword = focusNodes.get(1);
            JSONObject json = NumberColInstruction.build(keyword.getChildren(), formulas);
            String type = json.getString("type");
            if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
                expression.put("type", "column");
                Column column = (Column) json.get("column");
                expression.put("value", column.getColumnId());
                int begin = keyword.getChildren().getFirstNode().getBegin();
                int end = keyword.getChildren().getLastNode().getEnd();
                datas.addToken(AnnotationToken.singleCol(column, Constant.InstType.TABLE_COLUMN.equals(type), begin, end, amb));
            } else if (Constant.InstType.FUNCTION.equals(type)) {
                expression = json.getJSONObject(Constant.InstType.FUNCTION);
            }
            AnnotationToken token1 = new AnnotationToken();
            token1.addToken(first.getValue());
            token1.value = first.getValue();
            token1.type = key + "N";
            token1.begin = first.getBegin();
            token1.end = first.getEnd();
            datas.addToken(token1);
        }
        json1.put("n", 1);

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

    //    <number-columns> <top-n-chinese> <integer> <top-bottom-n-measure-word-chinese>
    private static JSONArray build2(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, String key) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", String.format("set_%s_n", key));
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, key);

        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();
        JSONObject expression = new JSONObject();
        FocusPhrase numberCol = focusNodes.get(0).getChildren();
        JSONObject json = NumberColInstruction.build(numberCol, formulas);
        String type = json.getString("type");
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            expression.put("type", "column");
            Column column = (Column) json.get("column");
            expression.put("value", column.getColumnId());
        } else if (Constant.InstType.FUNCTION.equals(type)) {
            expression = json.getJSONObject(Constant.InstType.FUNCTION);
        }
        datas.addTokens(NumberColInstruction.tokens(numberCol, formulas, amb));
        json1.put("expression", expression);

        FocusNode keyword = focusNodes.get(1).getChildren().getFirstNode();
        AnnotationToken token2 = new AnnotationToken();
        token2.addToken(keyword.getValue());
        token2.value = keyword.getValue();
        token2.type = key + "N";
        token2.begin = keyword.getBegin();
        token2.end = keyword.getEnd();
        datas.addToken(token2);

        FocusNode integer = focusNodes.get(2);
        datas.addToken(NumberArg.token(integer));
        json1.put("n", NumberArg.arg(integer).getInteger("value"));

        FocusNode measureWord = focusNodes.get(3).getChildren().getFirstNode();
        AnnotationToken token4 = new AnnotationToken();
        token4.addToken(measureWord.getValue());
        token4.value = measureWord.getValue();
        token4.type = key + "N";
        token4.begin = measureWord.getBegin();
        token4.end = measureWord.getEnd();
        datas.addToken(token4);

        instructions.add(json1);
        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", Constant.InstIdType.ANNOTATION);
        // annotation content
        json2.put("content", datas);
        instructions.add(json2);

        return instructions;

    }

    //    <top-n-chinese> <integer> <top-bottom-n-measure-word-chinese> <number-columns>
    private static JSONArray build3(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, String key) throws FocusInstructionException, IllegalException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", String.format("set_%s_n", key));
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE, key);

        List<FocusNode> focusNodes = focusPhrase.getFocusNodes();

        FocusNode keyword = focusNodes.get(0).getChildren().getFirstNode();
        AnnotationToken token1 = new AnnotationToken();
        token1.addToken(keyword.getValue());
        token1.value = keyword.getValue();
        token1.type = key + "N";
        token1.begin = keyword.getBegin();
        token1.end = keyword.getEnd();
        datas.addToken(token1);

        FocusNode integer = focusNodes.get(1);
        datas.addToken(NumberArg.token(integer));
        json1.put("n", NumberArg.arg(integer).getInteger("value"));

        FocusNode measureWord = focusNodes.get(2).getChildren().getFirstNode();
        AnnotationToken token3 = new AnnotationToken();
        token3.addToken(measureWord.getValue());
        token3.value = measureWord.getValue();
        token3.type = key + "N";
        token3.begin = measureWord.getBegin();
        token3.end = measureWord.getEnd();
        datas.addToken(token3);

        JSONObject expression = new JSONObject();
        FocusPhrase numberCol = focusNodes.get(3).getChildren();
        JSONObject json = NumberColInstruction.build(numberCol, formulas);
        String type = json.getString("type");
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            expression.put("type", "column");
            Column column = (Column) json.get("column");
            expression.put("value", column.getColumnId());
        } else if (Constant.InstType.FUNCTION.equals(type)) {
            expression = json.getJSONObject(Constant.InstType.FUNCTION);
        }
        datas.addTokens(NumberColInstruction.tokens(numberCol, formulas, amb));
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
