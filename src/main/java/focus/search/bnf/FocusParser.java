package focus.search.bnf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Constant;
import focus.search.bnf.exception.InvalidGrammarException;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.*;

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
        FocusInst fi = new FocusInst();
        fi.addPfs(parse(tokens));
        return fi;
    }

    private static List<FocusPhrase> parse(List<FocusToken> tokens) throws InvalidRuleException {
        FocusToken focusToken = tokens.get(0);

        List<FocusPhrase> focusPhrases = focusPhrases(focusToken);
        if (focusPhrases == null) {
            return null;
        }

        for (int i = 1; i < tokens.size(); i++) {
            FocusToken ft = tokens.get(i);
            List<BnfRule> rules = parseRules(ft.getWord());

            if (!replace(rules, focusPhrases, ft, i)) {
                System.out.println("*************error****************");
                System.out.println("index:" + i + " position:" + ft.getStart() + " token:" + JSON.toJSONString(ft));
                System.out.println("*************error****************");
                break;
            }
        }

        return focusPhrases;
    }

    private static boolean replace(List<BnfRule> rules, List<FocusPhrase> focusPhrases, FocusToken focusToken, int position) {
        List<FocusPhrase> tmp = new ArrayList<>();
        List<BnfRule> removes = new ArrayList<>();
        boolean match = false;
        while (!rules.isEmpty()) {
            int loop = focusPhrases.size();
            while (loop > 0 && !focusPhrases.isEmpty()) {
                FocusPhrase focusPhrase = focusPhrases.remove(0);
                if (focusPhrase.size() <= position) {
                    focusPhrase.setType(Constant.INSTRUCTION);
                    focusPhrases.add(focusPhrase);
                } else {
                    FocusNode fn = focusPhrase.getNode(position);
                    BnfRule br;
                    try {
                        br = findRule(rules, fn.getValue());
                    } catch (InvalidRuleException e) {
                        tmp.add(focusPhrase);
                        loop--;
                        continue;
                    }
                    removes.add(br);
                    for (TokenString ts : br.getAlternatives()) {
                        FocusPhrase newFp = new FocusPhrase(focusPhrase.getInstName());
                        newFp.addPns(focusPhrase.subNodes(0, position));
                        for (Token token : ts) {
                            FocusNode newFn = new FocusNode(token.getName());
                            newFn.setFt(focusToken);
                            newFn.setTerminal(true);
                            newFp.addPn(newFn);
                        }
                        newFp.addPns(focusPhrase.subNodes(position + 1));
                        if (newFp.size() == position + 1) {
                            newFp.setType(Constant.INSTRUCTION);
                        }
                        focusPhrases.add(newFp);
                    }
                }
                loop--;
            }

            // filter phrase
            if (!focusPhrases.isEmpty()) {// 部分规则能够匹配
                List<FocusPhrase> fps0 = new ArrayList<>();// 需要过滤掉的规则
                List<FocusPhrase> fps1 = new ArrayList<>();// 可能要过滤掉的规则
                match = false;
                for (FocusPhrase fp : focusPhrases) {
                    if (fp.size() < position) {
                        fps0.add(fp);
                    } else if (fp.size() == position) {
                        fps1.add(fp);
                    } else {
                        match = true;
                    }
                }
                focusPhrases.removeAll(fps0);
                fps0.clear();
                if (match) {
                    focusPhrases.removeAll(fps1);
                    fps1.clear();
                }
                tmp.clear();
            } else {// 没有规则能够匹配
                match = false;
                focusPhrases.clear();
                focusPhrases.addAll(tmp);
                break;
            }

            rules.removeAll(removes);
            removes.clear();
        }
        return match;
    }

    private static List<FocusPhrase> focusPhrases(FocusToken focusToken) throws InvalidRuleException {
        String word = focusToken.getWord();
        List<BnfRule> rules = parseRules(parser.getM_rules(), word);

        System.out.println("*****************************");
        System.out.println(JSON.toJSONString(rules));
        System.out.println("*****************************");

        if (rules.size() == 0) {
            return null;
        }
        BnfRule rule = rules.remove(0);
        List<FocusPhrase> focusPhrases = new ArrayList<>();
        List<BnfRule> removes = new ArrayList<>();


        if (rules.isEmpty()) {
            for (TokenString ts : rule.getAlternatives()) {
                FocusPhrase fp = new FocusPhrase();
                fp.setInstName(rule.getLeftHandSide().getName());
                for (Token token : ts) {
                    FocusNode fn = new FocusNode(token.getName());
                    fn.setFt(focusToken);
                    fn.setTerminal(true);
                    fp.addPn(fn);
                }
                focusPhrases.add(fp);
            }
        } else {
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
        }

        replace(rules, focusPhrases, focusToken, 0);

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

    public static List<BnfRule> parseRules(String word) throws InvalidRuleException {
        return parseRules(parser.getM_rules(), word);
    }

    private static List<BnfRule> parseRules(List<BnfRule> rules, String word) throws InvalidRuleException {
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
                    if (isTerminal(token.getName())) {
                        TokenString alternative_to_add = new TokenString();
                        alternative_to_add.add(new TerminalToken(word));
                        br.addAlternative(alternative_to_add);
                    } else {
                        br.addAlternative(alt);
                    }
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

    private static boolean isTerminal(String word) {
        return word.equals(IntegerTerminalToken.INTEGER) || word.equals(NumberTerminalToken.NUMBER) || word.startsWith("^");
    }

}
