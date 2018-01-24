package focus.search.bnf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusTokens;
import focus.search.bnf.exception.InvalidGrammarException;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.Token;
import focus.search.bnf.tokens.TokenString;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusParser {
    private static String file3 = System.getProperty("user.dir") + "/src/main/resources/bnf-file/my.bnf";
    private static BnfParser parser = null;

    static {
        init();
    }

    private static void init() {
        if (parser == null)
            try {
                parser = new BnfParser(new FileInputStream(file3));
            } catch (InvalidGrammarException | FileNotFoundException e) {
                e.printStackTrace();
            }
    }

    public static void addRule(BnfRule rule) {
        init();
        parser.addRule(rule);
    }

    public static List<TerminalToken> getTerminalTokens() {
        return parser.getTerminalTokens();
    }

    public static void parse(String question) throws IOException, InvalidRuleException {
        parse(question, "english");
    }

    private static void parse(String question, String language) throws IOException, InvalidRuleException {
        List<FocusTokens> tokens = FocusAnalyzer.test(question, language);
        for (FocusTokens ft : tokens) {
            Set<String> ams = ft.getAmbiguities();
            if (ams != null && ams.size() > 1) {
                System.out.println("ambiguity:\t" + ft.getStart() + "-" + ft.getEnd() + "," + ft.getWord() + "," + JSON.toJSONString(ams));
                return;
            }
        }
        parse(parser.getM_rules().getFirst(), tokens, question);
    }

    private static void parse(BnfRule rule, List<FocusTokens> tokens, String question) throws IOException, InvalidRuleException {
        FocusTokens ft = tokens.remove(0);
        List<BnfRule> rules = rules(rule, ft);
        for (BnfRule br : rules) {

        }
    }

    public static List<BnfRule> rules(FocusTokens ft) throws IOException, InvalidRuleException {
        return rules(parser.getM_rules().getFirst(), ft);
    }

    private static List<BnfRule> rules(BnfRule rule, FocusTokens ft) throws IOException, InvalidRuleException {
        List<BnfRule> rules = new ArrayList<>();
        String word = ft.getWord();
        for (TokenString alt : rule.getAlternatives()) {
            Iterator<Token> altIt = alt.iterator();
            BnfRule br = new BnfRule();
            br.setLeftHandSide(rule.getLeftHandSide());
            while (altIt.hasNext()) {
                Token token = altIt.next();
                // 如果为最小单元
                if (token instanceof TerminalToken) {
                    if (token.match(word)) {
                        br.addAlternative(alt);
                    }
                } else {
                    BnfRule newRule = getRule(token);
                    if (newRule == null) {
                        throw new InvalidRuleException("Cannot find rule for token " + JSONObject.toJSONString(token));
                    }
                    rules.addAll(rules(newRule, ft));
                }
            }
        }
        return rules;
    }

    private static BnfRule getRule(Token token) {
        return parser.getRule(token);
    }

}
