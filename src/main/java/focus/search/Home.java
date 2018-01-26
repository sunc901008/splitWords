package focus.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusToken;
import focus.search.bnf.BnfRule;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusParser;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.NonTerminalToken;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.TokenString;
import focus.search.meta.Column;

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
        // column info
        List<Column> columns = new ArrayList<>();
        Column col1 = new Column(42, "views", "int", "measure");
        col1.setTblName("USERS_ORACLE");
        columns.add(col1);
        Column col2 = new Column(41, "id", "int", "measure");
        col2.setTblName("USERS_ORACLE");
        columns.add(col2);
//        Column col3 = new Column(45, "displayname", "string", "attribute");
//        col3.setTblName("USERS_ORACLE");
//        columns.add(col3);

        // add split words
        FocusAnalyzer.addTable(columns);

        // add bnf rule
        for (Column col : columns) {
            BnfRule br0 = new BnfRule();
            br0.setLeftHandSide(new NonTerminalToken("<table-int-measure-column>"));
            TokenString alternative_to_add0 = new TokenString();
            alternative_to_add0.add(new TerminalToken(col.getTblName() + " " + col.getName()));
            br0.addAlternative(alternative_to_add0);
            FocusParser.addRule(br0);

            BnfRule br1 = new BnfRule();
            br1.setLeftHandSide(new NonTerminalToken("<int-measure-column>"));
            TokenString alternative_to_add1 = new TokenString();
            alternative_to_add1.add(new TerminalToken(col.getName()));
            br1.addAlternative(alternative_to_add1);
            FocusParser.addRule(br1);
        }

        String question = "views>6 id";

        String language = "english";
        List<FocusToken> tokens = FocusAnalyzer.test(question, language);

        System.out.println("分词:\n\t" + JSON.toJSONString(tokens) + "\n");

        List<TerminalToken> terminals = FocusParser.getTerminalTokens();
        System.out.println("最小单元词:\n\t" + JSON.toJSONString(terminals) + "\n");

//        System.out.println("------------------------");
//        BnfRule rule0 = FocusParser.getRule("<symbol>");
//
//        System.out.println("rule0:\n\t" + JSON.toJSONString(rule0) + "\n");


        System.out.println("------------------------");
        FocusInst focusInst = FocusParser.parse(question);
        System.out.println("解析:\n\t" + focusInst.toJSON().toJSONString() + "\n");

//        if (focusInst.isSuggestion()) {
//            Set<String> suggestions = new HashSet<>();
//            focusInst.getFocusPhrases().forEach(focusPhrase -> suggestions.add(focusPhrase.getFirstNode().getValue()));
//            System.out.println("------------------------");
//            System.out.println("提示:\n\t" + JSON.toJSONString(suggestions) + "\n");
//        }

//        System.out.println("------------------------");
//        List<BnfRule> rule = FocusParser.parseRules(">");
//        System.out.println(">:\n\t" + JSON.toJSONString(rule) + "\n");
//
//        System.out.println("------------------------");
//        List<BnfRule> rule1 = FocusParser.parseRules("4.0");
//        System.out.println("4.0:\n\t" + JSON.toJSONString(rule1) + "\n");
//
//        System.out.println("------------------------");
//        List<BnfRule> rule2 = FocusParser.parseRules("id");
//        System.out.println("id:\n\t" + JSON.toJSONString(rule2) + "\n");

//        for (BnfRule bnfRule : list) {
//            System.out.println(bnfRule.getLeftHandSide().getName());
//            bnfRule.getAlternatives().forEach(alt -> {
//                alt.forEach(ts -> {
//                    System.out.println("\t" + ts.getName());
//                });
//            });
//        }
/*
        int ubound = tokens.size() - 1;

        int error = -1;
        for (int i = 0; i < ubound; i++) {
            if (!isTerminal(terminals, tokens.get(i).getWord().toLowerCase())) {
                error = i;
                break;
            }
        }

        FocusToken last = tokens.get(ubound);
        List<String> sug = getSuggests(terminals, tokens.get(ubound).getWord().toLowerCase());

        if (sug.size() == 0 && !last.getType().equals("number")) {
            error = ubound;
        }

        for (String s : sug) {
            if (s.equalsIgnoreCase(tokens.get(ubound).getWord())) {
                sug.remove(s);
                break;
            }
        }

        if (error >= 0) {
            System.out.println("error token:\n\t" + JSON.toJSONString(tokens.get(error)) + "\n");
        } else if (sug.size() > 0) {
            System.out.println("suggest token:\n\t" + JSON.toJSONString(sug) + "\n");
        } else {
            System.out.println("post to bi data:\n\t" + search(question));
        }*/

    }

    private static boolean isTerminal(List<TerminalToken> terminals, String word) {
        for (TerminalToken tt : terminals) {
            if (tt.match(word)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> getSuggests(List<TerminalToken> terminals, String word) {
        List<String> res = new ArrayList<>();
        for (TerminalToken tt : terminals) {
            if (tt.getName().toLowerCase().startsWith(word)) {
                res.add(tt.getName());
            }
        }
        return res;
    }

    private static String search(String question) {
        JSONObject data = new JSONObject();
        data.put("query_type", "synchronize");
        data.put("question", question);

        JSONArray instructions = new JSONArray();

        JSONArray annotationId = new JSONArray();
        annotationId.add(1);

        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_simple_filter");
        json1.put("column", 42);
        json1.put("operator", ">");
        json1.put("value", 10);
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        instructions.add(json2);

        data.put("instructions", instructions);

//        String cmdStr = "cmd /c copy d:\\test.txt e:\\" ;
//        try {
//            Runtime.getRuntime().exec(cmdStr);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return data.toJSONString();

    }

}
