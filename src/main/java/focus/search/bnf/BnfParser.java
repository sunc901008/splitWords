package focus.search.bnf;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.exception.InvalidGrammarException;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.*;
import focus.search.response.exception.FocusParserException;

import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class BnfParser implements Serializable {

    /**
     * The list of parsing rules
     */
    private LinkedList<BnfRule> m_rules = new LinkedList<>();

    public BnfParser(InputStream is) throws InvalidGrammarException {
        Scanner s = new Scanner(is);
        setGrammar(s);
    }

    public LinkedList<BnfRule> getM_rules() {
        return this.m_rules;
    }

    public BnfRule getRule(String rule_name) {
        if (rule_name.startsWith(NonTerminalToken.s_leftSymbol) && !rule_name.endsWith(NonTerminalToken.s_rightSymbol)) {
            return null;
        }
        if (!rule_name.startsWith(NonTerminalToken.s_leftSymbol)) {
            rule_name = NonTerminalToken.s_leftSymbol + rule_name + NonTerminalToken.s_rightSymbol;
        }
        for (BnfRule rule : m_rules) {
            String lhs = rule.getLeftHandSide().getName();
            if (rule_name.compareTo(lhs) == 0) {
                return rule;
            }
        }
        return null;
    }

    public void setGrammar(String grammar) throws InvalidGrammarException {
        setGrammar(new Scanner(grammar));
    }

    public void setGrammar(Scanner scanner) throws InvalidGrammarException {
        List<BnfRule> rules = getRules(scanner);
        addRules(rules);
    }

    public static List<BnfRule> getRules(Scanner scanner) throws InvalidGrammarException {
        List<BnfRule> rules = new LinkedList<>();
        StringBuilder current_rule_builder = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // Remove comments and empty lines
            int index = line.indexOf('#');
            if (index >= 0) {
                line = line.substring(0, index);
            }
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            current_rule_builder.append(" ").append(line);
            if (line.endsWith(";")) {
                // We have a complete rule
                String current_rule = current_rule_builder.toString();
                try {
                    current_rule = current_rule.trim();
                    BnfRule new_rule = BnfRule.parseRule(current_rule.substring(0, current_rule.length() - 1));
                    rules.add(new_rule);
                } catch (InvalidRuleException e) {
                    scanner.close();
                    throw new InvalidGrammarException(e);
                }
                current_rule_builder.setLength(0);
            }
        }
        scanner.close();
        if (!current_rule_builder.toString().isEmpty()) {
            throw new InvalidGrammarException("Error parsing rule " + current_rule_builder.toString());
        }
        return rules;
    }

    public List<String> getAlternatives(String rule_name) {
        List<String> alternatives = new ArrayList<>();
        BnfRule rule = getRule(rule_name);
        if (rule != null) {
            for (TokenString alt : rule.getAlternatives()) {
                alternatives.add(alt.toString());
            }
        }
        return alternatives;
    }

    public void addRule(BnfRule rule) {
        NonTerminalToken r_left = rule.getLeftHandSide();
        boolean exist = false;
        for (BnfRule in_rule : m_rules) {
            NonTerminalToken in_left = in_rule.getLeftHandSide();
            if (r_left.equals(in_left)) {
                in_rule.addAlternatives(rule.getAlternatives());
                exist = true;
                break;
            }
        }
        // No rule with the same LHS was found
        if (!exist)
            m_rules.add(rule);
    }

    public void resetRule(BnfRule rule) {
        NonTerminalToken r_left = rule.getLeftHandSide();
        for (BnfRule in_rule : m_rules) {
            NonTerminalToken in_left = in_rule.getLeftHandSide();
            if (r_left.equals(in_left)) {
                in_rule.resetAlternatives(rule.getAlternatives());
                break;
            }
        }
    }

    private void addRules(Collection<BnfRule> rules) {
        for (BnfRule rule : rules) {
            addRule(rule);
        }
    }

    public BnfRule getRule(final Token tok) {
        if (tok == null) {
            return null;
        }
        for (BnfRule rule : m_rules) {
            NonTerminalToken lhs = rule.getLeftHandSide();
            if (lhs != null && lhs.getName().equals(tok.getName())) {
                return rule;
            }
        }
        return null;
    }

    public List<TerminalToken> getTerminalTokens() {
        List<TerminalToken> out = new ArrayList<>();
        List<String> exist = new ArrayList<>();
        for (BnfRule rule : m_rules) {
            for (TerminalToken tt : rule.getTerminalTokens()) {
                if (exist.contains(tt.getName())) {
                    continue;
                }
                exist.add(tt.getName());
                out.add(tt);
            }
        }
        return out;
    }

    public BnfRule parse(BnfRule rule, String word) throws FocusParserException {
        Map<String, Boolean> parseRules = new HashMap<>();
        BnfRule br = parse(parseRules, rule, word);
        parseRules.clear();
        return br;
    }

    private BnfRule parse(Map<String, Boolean> parseRules, BnfRule rule, String word) throws FocusParserException {
        BnfRule br = new BnfRule();
        br.setLeftHandSide(rule.getLeftHandSide());
        for (TokenString alt : rule.getAlternatives()) {
            Token token = alt.getFirst();
            if (token instanceof TerminalToken) {
                if (token.match(word)) {
                    if (isTerminal(token.getName())) {
                        TokenString alternative_to_add = new TokenString();
                        alternative_to_add.add(token);
                        br.addAlternative(alternative_to_add);
                    } else {
                        br.addAlternative(alt);
                    }
                }
            } else {
                BnfRule newBr = getRule(token);
                // 过滤公式|列规则|列中值
//                List<String> filter = Arrays.asList("<function-columns>", "<value>");
                if (newBr == null && !token.getName().endsWith("-column>")) {
                    throw new FocusParserException("Cannot find rule for tokens " + JSONObject.toJSONString(token));
                } else if (newBr != null) {
                    if (parseRules.containsKey(newBr.getLeftHandSide().getName())) {
                        if (parseRules.get(newBr.getLeftHandSide().getName())) {
                            br.addAlternative(alt);
                        }
                    } else if (newBr.equals(rule) || parse(parseRules, newBr, word) != null) {
                        parseRules.put(newBr.getLeftHandSide().getName(), true);
                        br.addAlternative(alt);
                    } else {
                        parseRules.put(newBr.getLeftHandSide().getName(), false);
                    }
                }
            }
        }
        if (br.getAlternatives().size() == 0) {
            return null;
        }
        return br;
    }

    // 判断当前匹配是否为最小单元词
    public boolean isTerminal(String word) {
        return word.equals(IntegerTerminalToken.INTEGER)
                || word.equals(NumberTerminalToken.DOUBLE)
                || word.equals(ColumnValueTerminalToken.COLUMN_VALUE)
                || word.equals(DateValueTerminalToken.DATE_VALUE);
    }

}