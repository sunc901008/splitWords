package focus.search;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.BnfParser;
import focus.search.bnf.BnfRule;
import focus.search.bnf.MutableString;
import focus.search.bnf.ParseNode;
import focus.search.bnf.exception.InvalidGrammarException;
import focus.search.bnf.tokens.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BnfTest {

    String file1 = System.getProperty("user.dir") + "/src/main/resources/bnf-file/Simple.bnf";
    String file2 = System.getProperty("user.dir") + "/src/main/resources/bnf-file/test.bnf";
    private static String file3 = System.getProperty("user.dir") + "/src/main/resources/bnf-file/my.bnf";
    private static BnfParser parser = null;

    static {
        try {
            parser = new BnfParser(new FileInputStream(file3));
        } catch (InvalidGrammarException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        String table1 = "{\"table\":\"USERS_ORACLE\", \"columns\":[\"id\",\"views\"]}";

        JSONObject json = JSON.parseObject(table1);
        String table = json.getString("table");
        JSONArray columns = json.getJSONArray("columns");

        for (Object obj : columns) {
            BnfRule br0 = new BnfRule();
            br0.setLeftHandSide(new NonTerminalToken("<table-int-attribute-column>"));
            TokenString alternative_to_add0 = new TokenString();
            alternative_to_add0.add(new TerminalToken(table + " " + obj));
            br0.addAlternative(alternative_to_add0);
            parser.addRule(br0);

            BnfRule br1 = new BnfRule();
            br1.setLeftHandSide(new NonTerminalToken("<int-attribute-column>"));
            TokenString alternative_to_add1 = new TokenString();
            alternative_to_add1.add(new TerminalToken(obj.toString()));
            br1.addAlternative(alternative_to_add1);
            parser.addRule(br1);
        }

//
        LinkedList<BnfRule> m_rules = parser.getM_rules();

        m_rules.forEach(rule -> {
            System.out.println(JSON.toJSONString(rule.getLeftHandSide()));
            System.out.println(JSON.toJSONString(rule.getAlternatives()));
            System.out.println(JSON.toJSONString(rule.getTerminalTokens()));
            System.out.println();
        });
//
        System.out.println();
        System.out.println(JSON.toJSONString(parser.getTerminalTokens()));

//        ParseNode pn = parse("views = 1 views");
//
//        System.out.println("----------------");
//        print(pn);

    }

    private static void print(ParseNode pn) {
        if (pn != null) {
            if (pn.getChildren().size() == 0)
                System.out.println(pn.getToken() + ":" + pn.getValue());
            else for (ParseNode child : pn.getChildren()) {
                System.out.print("\t");
                print(child);
            }
        }
    }

//    private static ParseNode parse(final String input) throws Exception {
//        MutableString n_input = new MutableString(input);
//        ParseNode n = parse(parser.getM_rules().peekFirst(), n_input, 0);
//        return n;
//    }

//    private static ParseNode parse(final BnfRule rule, MutableString input, int level) throws Exception {
//        if (level > 20) {
//            throw new Exception("Maximum number of recursion steps reached. If the input string is indeed valid, try increasing the limit.");
//        }
//        ParseNode out_node = new ParseNode();
//        boolean wrong_symbol = true;
//
//        MutableString n_input = new MutableString(input);
//
//        for (TokenString alt : rule.getAlternatives()) {
//            NonTerminalToken left_hand_side = rule.getLeftHandSide();
//            out_node.setToken(left_hand_side.toString());
//            out_node.setValue(left_hand_side.toString());
//            TokenString new_alt = alt.getCopy();
//            Iterator<Token> alt_it = new_alt.iterator();
//            n_input = new MutableString(input);
//            wrong_symbol = false;
//            while (alt_it.hasNext() && !wrong_symbol) {
//                n_input.trim();
//                Token alt_tok = alt_it.next();
//                if (alt_tok instanceof TerminalToken) {
//                    if (n_input.isEmpty()) {
//                        // Rule expects a token, string has no more: NO MATCH
//                        wrong_symbol = true;
//                        break;
//                    }
//                    int match_prefix_size = alt_tok.match(n_input.toString());
//                    if (match_prefix_size > 0) {
//                        ParseNode child = new ParseNode();
//                        MutableString input_tok = n_input.truncateSubstring(0, match_prefix_size);
//                        if (alt_tok instanceof RegexTerminalToken) {
//                            // In the case of a regex, create children with each capture block
//                            child = appendRegexChildren(child, (RegexTerminalToken) alt_tok, input_tok);
//                        }
//                        child.setToken(input_tok.toString());
//                        out_node.addChild(child);
//                    } else {
//                        // Rule expects a token, token in string does not match: NO MATCH
//                        wrong_symbol = true;
//                        out_node = new ParseNode();
//                        break;
//                    }
//                } else {
//                    ParseNode child;
//                    // Non-terminal token: recursively try to parse it
//                    String alt_tok_string = alt_tok.toString();
//                    BnfRule new_rule = getRule(alt_tok);
//                    if (new_rule == null) {
//                        // No rule found for non-terminal symbol:
//                        // there is an error in the grammar
//                        throw new Exception("Cannot find rule for token " + alt_tok);
//
//                    }
//                    child = parse(new_rule, n_input, level + 1);
//                    if (child == null) {
//                        // Parsing failed
//                        wrong_symbol = true;
//                        out_node = new ParseNode();
//                        break;
//                    }
//                    out_node.addChild(child);
//                }
//            }
//            if (!wrong_symbol) {
//                if (!alt_it.hasNext()) {
//                    // We succeeded in parsing the complete string: done
//                    if (level > 0 || (level == 0 && n_input.toString().trim().length() == 0)) {
//                        break;
//                    }
//                } else {
//                    // The rule expects more symbols, but there are none
//                    // left in the input; set wrong_symbol back to true to
//                    // force exploring the next alternative
//                    wrong_symbol = true;
//                    n_input = new MutableString(input);
//                    break;
//                }
//            }
//        }
//        int chars_consumed = input.length() - n_input.length();
//        if (wrong_symbol) {
//            // We did not consume anything, and the symbol was not epsilon: fail
//            return null;
//        }
//        if (chars_consumed == 0) {
//            // We did not consume anything, and the symbol was not epsilon: fail
//            return null;
//        }
//        input.truncateSubstring(chars_consumed);
//        if (level == 0 && !input.isEmpty()) {
//            // The top-level rule must parse the complete string
//            return null;
//        }
//        return out_node;
//    }

    private static ParseNode appendRegexChildren(ParseNode node, RegexTerminalToken tok, MutableString s) {
        List<String> blocks = tok.getCaptureBlocks(s.toString());
        for (String block : blocks) {
            ParseNode pn = new ParseNode(block);
            node.addChild(pn);
        }
        return node;
    }

    private static BnfRule getRule(final Token tok) {
        if (tok == null) {
            return null;
        }
        for (BnfRule rule : parser.getM_rules()) {
            NonTerminalToken lhs = rule.getLeftHandSide();
            if (lhs != null && lhs.getName().compareTo(tok.getName()) == 0) {
                return rule;
            }
        }
        return null;
    }

}
