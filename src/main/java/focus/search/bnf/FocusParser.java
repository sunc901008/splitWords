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
    private static final int MAX_RULE_LOOP = 5;

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
        List<FocusToken> copyTokens = new ArrayList<>(tokens);
        for (FocusToken ft : copyTokens) {
            Set<String> ams = ft.getAmbiguities();
            if (ams != null && ams.size() > 1) {
                System.out.println("ambiguity:\t" + ft.getStart() + "-" + ft.getEnd() + "," + ft.getWord() + "," + JSON.toJSONString(ams));
                return null;
            }
        }
        FocusInst fi = new FocusInst();
        int flag = 0;
        int position = 0;
        int error = 0;
        while (flag >= 0) {
            if (flag > 0) {
                copyTokens = copyTokens.subList(flag, copyTokens.size());
            }
            FocusSubInst fsi = parse(copyTokens);
            if (fsi == null) {
                fi.position = tokens.get(position).getStart();
                break;
            }
            flag = fsi.getIndex();
            fi.addPfs(fsi.getFps());
            error = position;
            position = position + flag;
        }
        if (error < position) {
            FocusSubInst fsi = parse(tokens.subList(error, position));
            assert fsi != null;
            fi.addPfs(fsi.getFps());
        }
        return fi;
    }

    private static FocusSubInst parse(List<FocusToken> tokens) throws InvalidRuleException {
        FocusToken focusToken = tokens.get(0);

        List<FocusPhrase> focusPhrases = focusPhrases(focusToken);
        if (focusPhrases == null) {
            return null;
        }

        for (int i = 1; i < tokens.size(); i++) {
            FocusToken ft = tokens.get(i);
            List<BnfRule> rules = parseRules(ft.getWord());
            List<FocusPhrase> tmp = new ArrayList<>(focusPhrases);
            replace(rules, focusPhrases, ft, i);
            if (same(tmp, focusPhrases)) {// 识别后的规则无变化，则表示识别结束
                FocusSubInst fsi = new FocusSubInst();
                fsi.setIndex(i);
                for (FocusPhrase fp : tmp) {
                    if (fp.size() == i) {
                        fsi.addFps(fp);
                    }
                }
                return fsi;
            } else {
                List<FocusPhrase> remove = new ArrayList<>();
                for (FocusPhrase fp : focusPhrases) {
                    if (fp.size() <= i) {
                        remove.add(fp);
                    }
                }
                focusPhrases.removeAll(remove);
            }

        }

        FocusSubInst fsi = new FocusSubInst();
        fsi.setIndex(-1);
        for (FocusPhrase fp : focusPhrases) {
            if (fp.size() == tokens.size()) {
                fsi.addFps(fp);
            }
        }
        if (fsi.isEmpty()) {
            for (FocusPhrase fp : focusPhrases) {
                if (fp.size() > tokens.size()) {
                    fsi.addFps(fp);
                }
            }
        }

//        FocusToken ft = tokens.get(tokens.size() - 1);
//        List<BnfRule> rules = parseRules(ft.getWord());
//        replace(rules, focusPhrases, ft, tokens.size() - 1);

        return fsi;
    }

    private static boolean same(List<FocusPhrase> o1, List<FocusPhrase> o2) {
        if (o1.size() != o2.size()) {
            return false;
        }
        for (int i = 0; i < o1.size(); i++) {
            if (!o1.get(i).toJSON().equals(o2.get(i).toJSON())) {
                return false;
            }
        }
        return true;
    }

    private static void replace(List<BnfRule> rules, List<FocusPhrase> focusPhrases, FocusToken focusToken, int position) {
        List<BnfRule> removes = new ArrayList<>();
        int max_rule = 1;
        while (!rules.isEmpty() && max_rule < MAX_RULE_LOOP) {
            max_rule++;
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
//            if (!focusPhrases.isEmpty()) {// 部分规则能够匹配
//                List<FocusPhrase> fps0 = new ArrayList<>();// 需要过滤掉的规则
//                List<FocusPhrase> fps1 = new ArrayList<>();// 可能要过滤掉的规则
//                boolean match = false;
//                for (FocusPhrase fp : focusPhrases) {
//                    if (fp.size() < position) {
//                        fps0.add(fp);
//                    } else if (fp.size() == position) {
//                        fps1.add(fp);
//                    } else {
//                        match = true;
//                    }
//                }
//                focusPhrases.removeAll(fps0);
//                fps0.clear();
//                if (match) {
//                    focusPhrases.removeAll(fps1);
//                    fps1.clear();
//                }
//                tmp.clear();
//            } else {// 没有规则能够匹配
//                focusPhrases.clear();
//                focusPhrases.addAll(tmp);
//                break;
//            }

            rules.removeAll(removes);
            removes.clear();
        }
    }

    private static List<FocusPhrase> focusPhrases(FocusToken focusToken) throws InvalidRuleException {
        String word = focusToken.getWord();
        List<BnfRule> rules = parseRules(parser.getM_rules(), word);

//        System.out.println("*****************************");
//        System.out.println(JSON.toJSONString(rules));
//        System.out.println("*****************************");

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
