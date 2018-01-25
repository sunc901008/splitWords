package focus.search.bnf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Constant;
import focus.search.bnf.exception.InvalidGrammarException;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.Token;
import focus.search.bnf.tokens.TokenString;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusParser {
    private static String file3 = System.getProperty("user.dir") + "/src/main/resources/bnf-file/test.bnf";
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

    private static BnfRule getRule(Token token) {
        return parser.getRule(token);
    }

    public static BnfRule getRule(String name) {
        return parser.getRule(name);
    }

    public static FocusInst parse(String question) throws IOException, InvalidRuleException {
        return parse(question, "english");
    }

    private static FocusInst parse(String question, String language) throws IOException, InvalidRuleException {
        List<FocusToken> tokens = FocusAnalyzer.test(question, language);
        for (FocusToken ft : tokens) {
            Set<String> ams = ft.getAmbiguities();
            if (ams != null && ams.size() > 1) {
                System.out.println("ambiguity:\t" + ft.getStart() + "-" + ft.getEnd() + "," + ft.getWord() + "," + JSON.toJSONString(ams));
                return null;
            }
        }
        return parse(tokens);
    }

    private static FocusInst parse(List<FocusToken> tokens) throws InvalidRuleException {
        FocusInst fi = new FocusInst();
        FocusToken focusToken = tokens.get(0);

        List<FocusPhrase> focusPhrases = focusPhrases(focusToken.getWord());
        if (focusPhrases == null) {
            return null;
        }

        fi.setFocusPhrases(focusPhrases);

        if (!fi.firstNode().getValue().equals(focusToken.getWord())) {
            fi.setType(Constant.SUGGESTION);
        }

        return fi;
    }

    private static List<FocusPhrase> focusPhrases(String word) throws InvalidRuleException {
        List<BnfRule> rules = parse(parser.getM_rules(), word);

        System.out.println("*****************************");
        System.out.println(JSON.toJSONString(rules));
        System.out.println("*****************************");

        if (rules.size() == 0) {
            return null;
        }
        BnfRule rule = rules.remove(0);
        List<FocusPhrase> focusPhrases = new ArrayList<>();
        List<BnfRule> removes = new ArrayList<>();
        for (TokenString alt : rule.getAlternatives()) {
            Token inst = alt.getFirst();
            BnfRule br = findRule(rules, inst);
            removes.add(br);
            for (TokenString ts : br.getAlternatives()) {
                FocusPhrase fp = new FocusPhrase();
                fp.setInstName(inst.getName());
                for (Token token : ts) {
                    FocusNode fn = new FocusNode(token.getName());
                    fp.addPn(fn);
                }
                focusPhrases.add(fp);
            }
        }
        rules.removeAll(removes);

        while (!rules.isEmpty()) {
            int loop = focusPhrases.size();
            while (loop > 0) {
                FocusPhrase focusPhrase = focusPhrases.remove(0);
                FocusNode fn = focusPhrase.getFirstNode();
                BnfRule br = findRule(rules, fn.getValue());
                removes.add(br);
                for (TokenString ts : br.getAlternatives()) {
                    FocusPhrase newFp = new FocusPhrase(focusPhrase.getInstName());
                    for (Token token : ts) {
                        FocusNode newFn = new FocusNode(token.getName());
                        newFp.addPn(newFn);
                    }
                    newFp.addPns(focusPhrase.subNodes(1));
                    focusPhrases.add(newFp);
                }
                loop--;
            }
            rules.removeAll(removes);
        }
        return focusPhrases;
    }

    private static BnfRule findRule(List<BnfRule> rules, Token token) throws InvalidRuleException {
        return findRule(rules, token.getName());
    }

    private static BnfRule findRule(List<BnfRule> rules, String token) throws InvalidRuleException {
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).getLeftHandSide().getName().equals(token)) {
                return rules.get(i);
            }
        }
        throw new InvalidRuleException("Cannot find rule for token " + JSONObject.toJSONString(token));
    }

    private static List<BnfRule> parse(List<BnfRule> rules, String word) throws InvalidRuleException {
        List<BnfRule> res = new ArrayList<>();
        for (BnfRule br : rules) {
            BnfRule rule = parse(br, word);
            if (rule != null) {
                res.add(rule);
            }
        }
        return res;
    }

    private static BnfRule parse(BnfRule rule, String word) throws InvalidRuleException {
        BnfRule br = new BnfRule();
        br.setLeftHandSide(rule.getLeftHandSide());
        for (TokenString alt : rule.getAlternatives()) {
            Token token = alt.getFirst();
            if (token instanceof TerminalToken) {
                if (token.match(word)) {
                    br.addAlternative(alt);
                }
            } else {
                BnfRule newBr = getRule(token);
                if (newBr == null && !token.getName().contains("column")) {
                    throw new InvalidRuleException("Cannot find rule for token " + JSONObject.toJSONString(token));
                } else if (newBr != null) {
                    if (parse(newBr, word) != null) {
                        br.addAlternative(alt);
                    }
                }
            }
        }
        if (br.getAlternatives().size() == 0) {
            return null;
        }
        return br;
    }

}
