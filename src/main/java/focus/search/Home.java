package focus.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.meta.Column;
import focus.search.response.exception.AmbiguitiesException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class Home {

    public static void main(String[] args) throws IOException, InvalidRuleException {
//        main();
//        System.out.println(JSON.toJSONString(ModelBuild.test(2)));
//
//        JSONObject json = sug();
//        String str = json.getJSONArray("suggestions").toJSONString();
//        System.out.println(str);
//        List<FocusNode> focusNodes = JSONArray.parseArray(str, FocusNode.class);
//        System.out.println(focusNodes);

//        List<AmbiguitiesRecord> aa = new ArrayList<>();
//        for (int i = 0; i < 3; i++) {
//            AmbiguitiesRecord ambiguitiesRecord = new AmbiguitiesRecord();
//            ambiguitiesRecord.sourceName = "table" + i;
//            ambiguitiesRecord.columnName = "column" + i;
//            ambiguitiesRecord.columnId = i + 1;
//            ambiguitiesRecord.type = Constant.FNDType.COLUMN;
//            aa.add(ambiguitiesRecord);
//        }
//
//        System.out.println(JSONObject.toJSONString(aa));
//
//        for (AmbiguitiesRecord a : aa) {
//            if (a.columnId == 2) {
//                aa.remove(a);
//                aa.add(0, a);
//                break;
//            }
//        }
//        System.out.println(JSONObject.toJSONString(aa));

    }

    private static JSONObject sug() {
        JSONObject json = new JSONObject();
        List<FocusNode> focusNodes = new ArrayList<>();
        FocusNode focusNode = new FocusNode();
        focusNode.setType("table");
        focusNodes.add(focusNode);
        FocusNode focusNode1 = new FocusNode();
        focusNode1.setType("column");
        Column column = new Column();
        column.setSourceName("users");
        column.setColumnId(1);
        focusNode1.setColumn(column);
        focusNodes.add(focusNode1);
        json.put("suggestions", focusNodes);
        return json;
    }

    public static void main() throws IOException, InvalidRuleException {

        FocusParser parser = new FocusParser();
        ModelBuild.build(parser, ModelBuild.test(2));

        String search = "views";
        List<FocusToken> tokens = parser.focusAnalyzer.test(search, "english");

//        List<String> keywords = FocusKWDict.getAllKeywords();
//        int loop = tokens.size();
//        while (loop > 0) {
//            FocusToken ft = tokens.remove(0);
//            if (keywords.contains(ft.getWord())) {
//                ft.setType("keyword");
//            }
//            tokens.add(ft);
//            loop--;
//        }

        System.out.println(JSON.toJSONString(tokens));
        FocusInst focusInst;
        try {
            focusInst = parser.parse(tokens, new JSONObject());
        } catch (AmbiguitiesException e) {
            System.out.println("Ambiguity:");
            System.out.println(e.toString());
            return;
        }
        System.out.println("-------------------");
        System.out.println(focusInst.toJSON());

        List<BnfRule> rules = parser.parseRules("users");
        System.out.println(JSON.toJSONString(rules));
        List<BnfRule> copyRules = new ArrayList<>(rules);
        for (BnfRule br : copyRules) {
            if (!isBaseRule(br, "users")) {
                rules.remove(br);
            }
        }

        System.out.println(JSON.toJSONString(rules));

        /*
        int position = focusInst.position;
        if (position < 0) {
            FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
            if (focusPhrase.isSuggestion()) {
                String msg = "提示:\n\t" + JSON.toJSONString(sug(tokens.size() - 1, focusInst)) + "\n";
                System.out.println(msg);
            } else {
                System.out.println("build instruction!");
            }
        } else {
            int strPosition = tokens.get(position).getStart();
            String msg = "错误:\n\t" + "位置: " + strPosition + "\t错误: " + search.substring(strPosition) + "\n";
            System.out.println(msg);
            msg = "提示:\n\t" + JSON.toJSONString(sug(position, focusInst)) + "\n";
            System.out.println(msg);
        }
        */

    }

    private static Set<String> sug(int position, FocusInst focusInst) {
        Set<String> suggestions = new HashSet<>();
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            if (fp.isSuggestion()) {
                suggestions.add(fp.getNode(position).getValue());
            } else {
                position = position - fp.size();
            }
        }
        return suggestions;
    }

    private static Boolean isBaseRule(BnfRule rule, String token) {
        for (TerminalToken tt : rule.getTerminalTokens()) {
            if (tt.getName().toLowerCase().startsWith(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
