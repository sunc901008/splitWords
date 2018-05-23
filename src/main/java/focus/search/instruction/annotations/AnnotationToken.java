package focus.search.instruction.annotations;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.meta.Formula;
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

    public static AnnotationToken singleCol(FocusPhrase fp, JSONObject amb) {
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
        // todo modify detailType
        token.detailType = column.getDataType();
        if (hasTable) {
            token.addToken(column.getSourceName());
        } else {
            logger.info("current all ambiguities:" + amb);
            for (String id : amb.keySet()) {
                AmbiguitiesResolve tmp = (AmbiguitiesResolve) amb.get(id);
                logger.info("current ambiguities:" + tmp.toJSON());
                if (tmp.ars.size() > 1 && tmp.value.equalsIgnoreCase(token.value.toString())) {
                    token.ambiguity = new AmbiguityDatas();
                    token.ambiguity.begin = token.begin;
                    token.ambiguity.end = token.end;
                    token.ambiguity.title = "ambiguity word: " + token.value;
                    token.ambiguity.id = id;
                    tmp.ars.forEach(a -> token.ambiguity.possibleMenus.add(a.columnName + " in table " + a.sourceName));
                    break;
                }
            }
        }
        token.addToken(column.getColumnDisplayName());
        logger.info("TEST: token:" + token.toJSON());
        return token;
    }

    public static JSONObject singleFormula(FocusNode node, Formula formula) {
        JSONObject json = new JSONObject();
        json.put("description", "formula " + formula.getName() + ":" + formula.getFormula());
        json.put("formula", formula.toJSON());
        json.put("type", Constant.FNDType.FORMULA);
        json.put("detailType", Constant.FNDType.FORMULA);
        json.put("value", formula.getName());
        json.put("begin", node.getBegin());
        json.put("end", node.getEnd());
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(formula.getName());
        json.put("tokens", jsonArray);
        return json;

    }

}
