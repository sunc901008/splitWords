package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusParser;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.meta.Column;

import java.util.List;

/**
 * user: sunc
 * data: 2018/1/27.
 */
public class SimpleInst {

    public static String simpleFilter(FocusInst focusInst, String question) throws InvalidRuleException {
        JSONObject data = new JSONObject();
        data.put("query_type", "synchronize");
        data.put("question", question);

        JSONArray instructions = new JSONArray();
        List<FocusPhrase> focusPhrases = focusInst.getFocusPhrases();
        for (int i = 0; i < focusPhrases.size(); i++) {
            JSONArray annotationId = new JSONArray();
            annotationId.add(i + 1);
            JSONObject json1 = new JSONObject();
            json1.put("annotationId", annotationId);
            json1.put("instId", "add_simple_filter");
            FocusPhrase focusPhrase = focusPhrases.get(i);
            Column col = getCol(focusPhrase.getNode(0).getValue());
            if(col == null){
                throw new InvalidRuleException("Build instruction fail!!!");
            }
            json1.put("column", col.getId());
            if (focusPhrase.size() == 3) {
                json1.put("operator", focusPhrase.getNode(1).getValue());
                json1.put("value", focusPhrase.getNode(2).getValue());
            }
            instructions.add(json1);

            JSONObject json2 = new JSONObject();
            json2.put("annotationId", annotationId);
            instructions.add(json2);
        }
        data.put("instructions", instructions);

        return data.toJSONString();
    }

    private static Column getCol(String colName) {
        List<TerminalToken> tokens = FocusParser.getTerminalTokens();
        for (TerminalToken ter : tokens) {
            if (ter.getName().equalsIgnoreCase(colName) && ter.getColumn() != null) {
                return ter.getColumn();
            }
        }
        return null;
    }

}
