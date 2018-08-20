package focus.search.instruction.annotations;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.CommonFunc;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.search.AmbiguityDatas;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/24
 * description:
 */
public class AnnotationToken {
    private static final Logger logger = Logger.getLogger(AnnotationToken.class);

    public String description;
    public String columnName;
    public String tableName;
    public Integer columnId;
    public String type;
    public String detailType;
    public Object value;
    public Integer begin;
    public Integer end;
    public List<String> tokens;
    public AmbiguityDatas ambiguity;

    public void addToken(String token) {
        if (this.tokens == null) {
            this.tokens = new ArrayList<>();
        }
        this.tokens.add(token);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("description", this.description);
        json.put("columnName", this.columnName);
        json.put("tableName", this.tableName);
        json.put("columnId", this.columnId);
        json.put("type", this.type);
        json.put("detailType", this.detailType);
        json.put("value", this.value);
        json.put("begin", this.begin);
        json.put("columnId", this.columnId);
        json.put("end", this.end);
        if (this.tokens != null) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(this.tokens);
            json.put("tokens", jsonArray);
        }
        if (ambiguity != null)
            json.put("ambiguity", ambiguity.toJSON());
        return json;
    }

    // column or formula column
    public static AnnotationToken singleCol(FocusPhrase fp, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        FocusNode formulaNode = fp.getFirstNode();
        if (Constant.FNDType.FORMULA.equals(formulaNode.getType())) {
            return singleFormula(formulaNode, formulas);
        }
        int begin = fp.getFirstNode().getBegin();
        int end = fp.getLastNode().getEnd();
        Column column = fp.getLastNode().getColumn();
        return singleCol(column, fp.size() == 2, begin, end, amb);
    }

    public static AnnotationToken singleCol(Column column, boolean hasTable, int begin, int end, JSONObject amb) {
        AnnotationToken token = new AnnotationToken();
        token.description = "column " + column.getColumnDisplayName() + " in " + column.getSourceName();
        token.tableName = column.getSourceName();
        token.columnName = column.getColumnDisplayName();
        token.columnId = column.getColumnId();
        token.type = column.getColumnType().toLowerCase();
        token.value = column.getColumnDisplayName();
        token.begin = begin;
        token.end = end;
        token.detailType = column.getDataType();
        if (hasTable) {
            token.addToken(column.getSourceName());
        } else {
            logger.debug("current all ambiguities:" + amb);
            token.ambiguity = getAmbiguityDatas(amb, token.value.toString(), token.begin, token.end);
        }
        token.addToken(column.getColumnDisplayName());
        logger.debug("TEST: tokens:" + token.toJSON());
        return token;
    }

    public static AnnotationToken singleFormula(FocusNode node, List<Formula> formulas) throws FocusInstructionException {
        Formula formula = CommonFunc.getFormula(formulas, node.getValue());
        JSONObject json = new JSONObject();
        json.put("description", formula.getName() + ":" + formula.getFormula());
        json.put("formula", formula.toJSON());
        json.put("type", Constant.FNDType.FORMULA);
        json.put("detailType", Constant.FNDType.FORMULA);
        json.put("value", formula.getName());
        json.put("begin", node.getBegin());
        json.put("end", node.getEnd());
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(formula.getName());
        json.put("tokens", jsonArray);
        return JSONObject.parseObject(json.toJSONString(), AnnotationToken.class);

    }

    public static AmbiguityDatas getAmbiguityDatas(JSONObject amb, String value, int begin, int end) {
        return getAmbiguityDatas(amb, value, value, begin, end);
    }

    public static AmbiguityDatas getAmbiguityDatas(JSONObject amb, String value, String title, int begin, int end) {
        for (String id : amb.keySet()) {
            AmbiguitiesResolve tmp = (AmbiguitiesResolve) amb.get(id);
            if (tmp.ars.size() > 1 && tmp.value.equalsIgnoreCase(value)) {
                AmbiguityDatas ambiguity = new AmbiguityDatas();
                ambiguity.begin = begin;
                ambiguity.end = end;
                ambiguity.title = "ambiguity word: " + title;
                ambiguity.id = id;
                tmp.ars.forEach(a -> ambiguity.possibleMenus.add(a.columnName + " in table " + a.sourceName));
                return ambiguity;
            }
        }
        return null;
    }

}
