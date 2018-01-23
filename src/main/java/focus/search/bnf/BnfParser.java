package focus.search.bnf;


import focus.search.bnf.exception.InvalidGrammarException;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.NonTerminalToken;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.Token;
import focus.search.bnf.tokens.TokenString;

import java.io.InputStream;
import java.io.Serializable;
import java.util.*;


public class BnfParser implements Serializable {
    /**
     * Dummy UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The list of parsing rules
     */
    private LinkedList<BnfRule> m_rules;


    /**
     */
    public BnfParser() {
        super();
        m_rules = new LinkedList<>();
    }

    /**
     * Creates a new parser by reading its grammar from the contents of
     * some input stream
     *
     * @param is The input stream to read from
     * @throws InvalidGrammarException Thrown if the grammar to read is invalid
     */
    public BnfParser(InputStream is) throws InvalidGrammarException {
        this();
        Scanner s = new Scanner(is);
        setGrammar(s);
    }

    public LinkedList<BnfRule> getM_rules() {
        return m_rules;
    }

    protected BnfRule getRule(String rule_name) {
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
        List<BnfRule> rules = getRules(grammar);
        addRules(rules);
    }

    /**
     */
    public void setGrammar(Scanner scanner) throws InvalidGrammarException {
        List<BnfRule> rules = getRules(scanner);
        addRules(rules);
    }


    /**
     * Converts a string into a list of grammar rules
     *
     * @param grammar The string containing the grammar to be used
     * @return A list of grammar rules
     * @throws InvalidGrammarException Thrown if the grammar string is invalid
     */
    public static List<BnfRule> getRules(String grammar) throws InvalidGrammarException {
        if (grammar == null) {
            throw new InvalidGrammarException("Null argument given");
        }
        Scanner s = new Scanner(grammar);
        return getRules(s);
    }

    /**
     */
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

    /**
     * Returns the list of alternative rules
     *
     * @param rule_name The rule you need the alternatives
     * @return a list of strings representing the alternatives
     */
    public /*@NotNull*/ List<String> getAlternatives(String rule_name) {
        for (BnfRule rule : m_rules) {
            String lhs = rule.getLeftHandSide().getName();
            if (rule_name.compareTo(lhs) == 0) {
                List<String> alternatives = new ArrayList<>();
                for (TokenString alt : rule.getAlternatives()) {
                    alternatives.add(alt.toString());
                }
                return alternatives;
            }
        }
        return new ArrayList<>(0);
    }

    /**
     */
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

    /**
     */
    public void addRules(Collection<BnfRule> rules) {
        for (BnfRule rule : rules) {
            addRule(rule);
        }
    }

    private BnfRule getRule(final Token tok) {
        if (tok == null) {
            return null;
        }
        for (BnfRule rule : m_rules) {
            NonTerminalToken lhs = rule.getLeftHandSide();
            if (lhs != null && lhs.toString().compareTo(tok.toString()) == 0) {
                return rule;
            }
        }
        return null;
    }

    public List<TerminalToken> getTerminalTokens() {
        List<TerminalToken> out = new ArrayList<>();
        for (BnfRule rule : m_rules) {
            out.addAll(rule.getTerminalTokens());
        }
        return out;
    }

}