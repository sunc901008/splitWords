package focus.search.instruction;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.search.AmbiguityResponse;
import focus.search.response.search.AnnotationResponse;

/**
 * creator: sunc
 * date: 2018/3/9
 * description:
 */
public class AnnotationBuild {

    public static AnnotationResponse.Datas build(FocusPhrase focusPhrase, int index, JSONObject amb) {
        return build(focusPhrase, index, amb, null);
    }

    public static AnnotationResponse.Datas build(FocusPhrase focusPhrase, int index, JSONObject amb, Formula formula) {
        AnnotationResponse.Datas datas = new AnnotationResponse.Datas();
        datas.id = index;
        datas.begin = focusPhrase.getFirstNode().getBegin();
        datas.end = focusPhrase.getLastNode().getEnd();
        String instName = focusPhrase.getInstName();
        if (instName.equals("<all-column>")) {
            // todo category and type
            datas.type = "phrase";
            boolean hasTable = false;
            int colPosition = focusPhrase.size() - 1;
            if (focusPhrase.size() == 2) {
                hasTable = true;
            }
            if (formula != null) {
                datas.category = "formulaName";
                datas.tokens.add(singleFormula(focusPhrase.getFirstNode(), formula));
            } else {
                datas.category = "column";
                datas.tokens.add(singleColumn(focusPhrase, hasTable, colPosition, amb));
            }

            return datas;
        }
        if (instName.equals("<top-n>")) {
            // todo category and type
            datas.type = "filter";
            datas.category = "";
            boolean hasTable = false;
            int colPosition = focusPhrase.size() - 1;
            if (focusPhrase.getNode(colPosition - 1).getType().equals(Constant.FNDType.TABLE)) {
                hasTable = true;
            }
            AnnotationResponse.Tokens token = singleColumn(focusPhrase, hasTable, colPosition, amb);
            datas.tokens.add(token);
            return datas;
        }
        if (instName.equals("<simple-filter>")) {
            // todo category and type
            datas.type = "filter";
            datas.category = "";
            boolean hasTable = false;
            int colPosition = 0;
            if (focusPhrase.getFirstNode().getType().equals(Constant.FNDType.TABLE)) {
                hasTable = true;
                colPosition = 1;
            }
            AnnotationResponse.Tokens token1 = singleColumn(focusPhrase, hasTable, colPosition, amb);
            datas.tokens.add(token1);

            FocusNode operatorNode = focusPhrase.getNode(++colPosition);
            AnnotationResponse.Tokens token2 = new AnnotationResponse.Tokens();
            token2.type = operatorNode.getType();
            token2.value = operatorNode.getValue();
            token2.begin = operatorNode.getBegin();
            token2.end = operatorNode.getEnd();
            datas.tokens.add(token2);

            FocusNode numberNode = focusPhrase.getNode(++colPosition);
            AnnotationResponse.Tokens token3 = new AnnotationResponse.Tokens();
            token3.type = numberNode.getType();
            token3.value = numberNode.getValue();
            token3.begin = numberNode.getBegin();
            token3.end = numberNode.getEnd();
            datas.tokens.add(token3);

            return datas;
        }
        return datas;
    }


    /**
     * @param focusPhrase list focusNode
     * @param hasTable    contains table or not
     * @param colPosition column position in focusPhrase
     * @return AnnotationResponse.Tokens
     */
    private static AnnotationResponse.Tokens singleColumn(FocusPhrase focusPhrase, boolean hasTable, int colPosition, JSONObject amb) {
        FocusNode columnNode = focusPhrase.getNode(colPosition);
        Column column = columnNode.getColumn();
        AnnotationResponse.Tokens token = new AnnotationResponse.Tokens();
        token.description = column.getSourceName() + " " + column.getColumnDisplayName();
        token.columnId = column.getColumnId();
        token.columnName = column.getColumnDisplayName();
        // todo modify detailType
        token.detailType = column.getDataType();
        token.tableName = column.getSourceName();
        token.type = column.getColumnType().toLowerCase();
        token.value = column.getColumnDisplayName();
        token.begin = columnNode.getBegin();
        token.end = columnNode.getEnd();
        if (hasTable) {
            token.begin = focusPhrase.getNode(colPosition - 1).getBegin();
            token.tokens.add(column.getSourceName());
        } else {
            for (String id : amb.keySet()) {
                AmbiguitiesResolve tmp = (AmbiguitiesResolve) amb.get(id);
                if (tmp.value.equalsIgnoreCase(token.value)) {
                    token.ambiguity = new AmbiguityResponse.Datas();
                    token.ambiguity.begin = token.begin;
                    token.ambiguity.end = token.end;
                    token.ambiguity.title = "ambiguity word: " + token.value;
                    token.ambiguity.id = id;
                    tmp.ars.forEach(a -> token.ambiguity.possibleMenus.add(a.columnName + " in table " + a.sourceName));
                    break;
                }
            }
        }
        token.tokens.add(column.getColumnDisplayName());
        return token;
    }

    private static AnnotationResponse.FormulaTokens singleFormula(FocusNode node, Formula formula) {
        AnnotationResponse.FormulaTokens token = new AnnotationResponse.FormulaTokens();
        token.begin = node.getBegin();
        token.end = node.getEnd();
        token.formula = formula;
        token.description = "formula " + formula.getName() + ":" + formula.getFormula();
        token.type = "formulaName";
        token.detailType = "formulaName";
        token.tokens.add(formula.getName());
        token.value = formula.getName();
        return token;
    }

}
