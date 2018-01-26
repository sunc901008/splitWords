package focus.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusToken;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.NonTerminalToken;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.TokenString;
import focus.search.instruction.SimpleInst;
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
            alternative_to_add0.add(new TerminalToken(col.getTblName() + " " + col.getName(), col));
            br0.addAlternative(alternative_to_add0);
            FocusParser.addRule(br0);

            BnfRule br1 = new BnfRule();
            br1.setLeftHandSide(new NonTerminalToken("<int-measure-column>"));
            TokenString alternative_to_add1 = new TokenString();
            alternative_to_add1.add(new TerminalToken(col));
            br1.addAlternative(alternative_to_add1);
            FocusParser.addRule(br1);
        }

        String question = "id>6 views>3";

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

        if (focusInst.position < 0) {
            FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
            if (focusPhrase.isSuggestion()) {
                int sug = 0;
                while (sug < focusPhrase.size()) {
                    FocusNode tmpNode = focusPhrase.getNode(sug);
                    if (!tmpNode.isTerminal()) {
                        System.out.println("------------------------");
                        System.out.println("提示:\n\t" + tmpNode.getValue() + "\n");
                        return;
                    }
                    sug++;
                }

            } else {
                System.out.println("------------------------");
                System.out.println("指令:\n\t" + SimpleInst.simpleFilter(focusInst, question) + "\n");
            }
        } else {
            System.out.println("------------------------");
            System.out.println("错误:\n\t" + question.substring(focusInst.position) + "\n");
            FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
            int sug = 0;
            while (sug < focusPhrase.size()) {
                FocusNode tmpNode = focusPhrase.getNode(sug);
                if (!tmpNode.isTerminal()) {
                    System.out.println("------------------------");
                    System.out.println("应该输入:\n\t" + tmpNode.getValue() + "\n");
                    return;
                }
                sug++;
            }
        }

    }

}
