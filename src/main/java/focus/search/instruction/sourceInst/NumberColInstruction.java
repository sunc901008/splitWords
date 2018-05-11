package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.controller.common.FormulaAnalysis;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.NumberFuncInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */

//<number-columns> := <all-int-column> |
//        <all-double-column> |
//        <number-function-column> |
//        ( <number-function-column> );
public class NumberColInstruction {
    private static final Logger logger = Logger.getLogger(NumberFuncInstruction.class);

    // 完整指令 columns
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        logger.info("NumberColumns instruction build. focusPhrase:" + focusPhrase.toJSON());
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_expression");

        JSONObject expression = new JSONObject();
        JSONObject json = build(focusPhrase, formulas);
        logger.debug(json);
        String type = json.getString("type");
        if (Constant.InstType.TABLE_COLUMN.equals(type) || Constant.InstType.COLUMN.equals(type)) {
            expression.put("type", "column");
            Column column = (Column) json.get("column");
            expression.put("value", column.getColumnId());
            if (Constant.ColumnType.MEASURE.equals(column.getColumnType())) {
                json1.put("instId", "add_column_for_measure");
            } else {
                json1.put("instId", "add_column_for_group");
            }
        } else if (Constant.InstType.FUNCTION.equals(type)) {
            expression = json.getJSONObject(Constant.InstType.FUNCTION);
        }

        json1.put("expression", expression);
        instructions.add(json1);
        logger.debug(instructions);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        AnnotationDatas datas = new AnnotationDatas(focusPhrase, index, Constant.AnnotationType.PHRASE);

        if ("<number-function-column>".equals(focusPhrase.getFocusNodes().get(0).getValue())) {
            datas.category = Constant.AnnotationCategory.EXPRESSION;
        } else {
            datas.category = Constant.AnnotationCategory.MEASURE_COLUMN;
        }

        datas.addTokens(tokens(focusPhrase, formulas, amb));
        json2.put("content", datas);

        instructions.add(json2);

        return instructions;
    }

    // 其他指令的一部分
    public static JSONObject build(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        JSONObject res = new JSONObject();
        switch (node.getValue()) {
            case "<all-int-column>":
            case "<all-double-column>":
                JSONObject json = ColumnInstruction.build(node.getChildren());
                if (json.getBoolean("hasTable")) {
                    res.put("type", Constant.InstType.TABLE_COLUMN);
                } else {
                    res.put("type", Constant.InstType.COLUMN);
                }
                res.put("column", json.get("column"));
                return res;
            case "<number-function-column>":
                res.put("type", Constant.InstType.FUNCTION);
                res.put("function", NumberFuncInstruction.arg(node.getChildren(), formulas));
                return res;
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode node = focusPhrase.getFocusNodes().get(0);
        List<AnnotationToken> tokens = new ArrayList<>();
        switch (node.getValue()) {
            case "<all-int-column>":
            case "<all-double-column>":
                tokens.add(AnnotationToken.singleCol(node.getChildren(), amb));
                return tokens;
            case FormulaAnalysis.LEFT_BRACKET:
                AnnotationToken token1 = new AnnotationToken();
                token1.value = node.getValue();
                token1.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
                token1.begin = node.getBegin();
                token1.end = node.getEnd();
                tokens.add(token1);

                tokens.addAll(NumberFuncInstruction.tokens(focusPhrase.getFocusNodes().get(1).getChildren(), formulas, amb));

                AnnotationToken token2 = new AnnotationToken();
                token2.value = focusPhrase.getFocusNodes().get(2).getValue();
                token2.type = Constant.AnnotationTokenType.PUNCTUATION_MARK;
                token2.begin = focusPhrase.getFocusNodes().get(2).getBegin();
                token2.end = focusPhrase.getFocusNodes().get(2).getEnd();
                tokens.add(token2);

                return tokens;
            case "<number-function-column>":
                return NumberFuncInstruction.tokens(node.getChildren(), formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
