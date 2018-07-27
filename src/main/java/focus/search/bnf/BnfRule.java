package focus.search.bnf;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.*;
import focus.search.response.exception.FocusParserException;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a single rule of a BNF grammar
 */
public class BnfRule implements Serializable {
    private static final long serialVersionUID = 579372107761975753L;

    /**
     * A list of tokens strings that for all the possible cases of that rule
     */
    private List<TokenString> alternatives = new ArrayList<>();

    /**
     * The left-hand side of the rule. Since we deal with BNF grammars, this
     * left-hand side must be a single non-terminal symbol.
     */
    private NonTerminalToken leftHandSide;

    /**
     * Creates a new empty BNF rule
     */
    public BnfRule() {
    }

    public void setAlternatives(List<TokenString> alternatives) {
        this.alternatives = alternatives;
    }

    /**
     * Creates a BNF rule out of a string
     */
    static BnfRule parseRule(String input) throws InvalidRuleException {
        BnfRule out = new BnfRule();
        String[] lr = input.split("\\s*:=\\s*");
        String lhs = lr[0].trim();
        out.setLeftHandSide(new NonTerminalToken(lhs));
        if (lhs.equals(NumberTerminalToken.DOUBLE)) {
            TokenString alternative_to_add = new TokenString();
            alternative_to_add.add(new NumberTerminalToken(lhs));
            out.addAlternative(alternative_to_add);
            return out;
        }
        if (lhs.equals(IntegerTerminalToken.INTEGER)) {
            TokenString alternative_to_add = new TokenString();
            alternative_to_add.add(new IntegerTerminalToken(lhs));
            out.addAlternative(alternative_to_add);
            return out;
        }
        if (lhs.equals(ColumnValueTerminalToken.COLUMN_VALUE)) {
            TokenString alternative_to_add = new TokenString();
            alternative_to_add.add(new ColumnValueTerminalToken(lhs));
            out.addAlternative(alternative_to_add);
            return out;
        }
        if (lhs.equals(DateValueTerminalToken.DATE_VALUE)) {
            TokenString alternative_to_add = new TokenString();
            alternative_to_add.add(new DateValueTerminalToken(lhs));
            out.addAlternative(alternative_to_add);
            return out;
        }
        if (lr.length != 2) {
            throw new InvalidRuleException("Cannot find left- and right-hand side of BNF rule");
        }
//        if (lr[1].startsWith("^") && !lr[1].equals("^")) {
//            // This is a regex line
//            String regex = unescapeString(lr[1]);
//            TokenString alternative_to_add = new TokenString();
//            Token to_add = new RegexTerminalToken(regex);
//            alternative_to_add.add(to_add);
//            out.addAlternative(alternative_to_add);
//        } else {
//            // Anything but a regex line
//            String[] parts = lr[1].split("\\s*\\|\\s*");
//            if (parts.length <= 0) {
//                throw new InvalidRuleException("Right-hand side of BNF rule is empty");
//            }
//            for (String part : parts)
//                processAlternatives(out, part);
//        }

        String[] parts = lr[1].split("\\s*\\|\\s*");
        if (parts.length <= 0) {
            throw new InvalidRuleException("Right-hand side of BNF rule is empty");
        }
        for (String part : parts)
            processAlternatives(out, part);
        return out;
    }

    private static void processAlternatives(BnfRule out, String alt) throws InvalidRuleException {
        TokenString alternative_to_add = new TokenString();
        String[] words = alt.split(" ");
        if (words.length == 0) {
            throw new InvalidRuleException("Alternative of BNF rule is empty");
        }
        assert words.length > 0;
        for (String word : words) {
            String trimmed_word = word.trim();
            if (trimmed_word.isEmpty()) {
                throw new InvalidRuleException("Trying to create an empty terminal tokens");
            }
            if (trimmed_word.equals(NonTerminalToken.s_leftSymbol) || trimmed_word.equals("<=")) {
                alternative_to_add.add(new TerminalToken(trimmed_word, Constant.FNDType.SYMBOL));
                continue;
            }
            if (trimmed_word.startsWith(NonTerminalToken.s_leftSymbol) && !trimmed_word.endsWith(NonTerminalToken.s_rightSymbol)) {
                throw new InvalidRuleException("The expression '" + trimmed_word + "' is a wrong rule");
            }
            if (trimmed_word.startsWith(NonTerminalToken.s_leftSymbol)) {
                // This is a non-terminal symbol
                Token to_add = new NonTerminalToken(trimmed_word);
                alternative_to_add.add(to_add);

            } else {
                // This is a literal tokens
                trimmed_word = unescapeString(trimmed_word);
                String type = Constant.FNDType.KEYWORD;
                String left = out.getLeftHandSide().getName();
                if (left.equals("<bool-symbol>") || left.equals("<math-symbol>")) {
                    type = Constant.FNDType.SYMBOL;
                }
                alternative_to_add.add(new TerminalToken(trimmed_word, type));
            }
        }
        out.addAlternative(alternative_to_add);

    }

    /**
     * Sets the left-hand side of the rule
     *
     * @param t The non-terminal tokens that will be used for the left-hand side of the rule
     */
    public void setLeftHandSide(final NonTerminalToken t) {
        leftHandSide = t;
    }

    /**
     * Adds an alternative to the rule
     *
     * @param ts The alternative to add
     */
    public void addAlternative(final TokenString ts) {
        if (!exist(ts)) {
            alternatives.add(ts);
        }
    }

    /**
     * Retrieves the list of all the alternatives that this rule defines
     *
     * @return A list of alternatives, each of which is a string of tokens (either terminal or non-terminal)
     */
    public List<TokenString> getAlternatives() {
        return alternatives;
    }

    /**
     * Retrieves the left-hand side symbol of the rule
     *
     * @return The left-hand side symbol
     */
    public NonTerminalToken getLeftHandSide() {
        return leftHandSide;
    }

    // transfer UNICODE
    // Dodd\u2013Frank | Dodd\\u2013Frank  ->  Dodd–Frank
    protected static String unescapeString(String s) {
        String new_s = s.replaceAll("\\\\([^u])", "\\\\\\\\$1");
        Properties p = new Properties();
        try {
            p.load(new StringReader("key=" + new_s));
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "", e);
        }
        return p.getProperty("key", new_s);
    }

    // 获取当前bnf下的所有 terminalToken
    public List<TerminalToken> getTerminalTokens() {
        List<TerminalToken> out = new ArrayList<>();
        for (TokenString ts : alternatives) {
            out.addAll(ts.getTerminalTokens());
        }
        return out;
    }

    // 获取当前bnf下以及子bnf的所有 terminalToken
    public List<TerminalToken> getAllTerminalTokens(List<BnfRule> rules) {
        List<String> ruleNames = new ArrayList<>();
        List<TerminalToken> tokens = getAllTerminalTokens(ruleNames, rules);
        ruleNames.clear();
        return tokens;
    }

    public void addAlternatives(Collection<TokenString> alternatives) {
        for (TokenString ts : alternatives) {
            if (!exist(ts)) {
                this.alternatives.add(ts);
            }
        }
    }

    // 获取当前bnf下以及子bnf的所有 terminalToken
    private List<TerminalToken> getAllTerminalTokens(List<String> ruleNames, List<BnfRule> rules) {
        List<TerminalToken> out = new ArrayList<>();
        for (TokenString ts : alternatives) {
            for (Token t : ts) {
                if (ruleNames.contains(t.getName())) {
                    continue;
                }
                ruleNames.add(t.getName());
                if (t instanceof TerminalToken) {
                    out.add((TerminalToken) t);
                } else {
                    try {
                        BnfRule r = FocusParser.findRule(rules, t);
                        out.addAll(r.getAllTerminalTokens(ruleNames, rules));
                    } catch (FocusParserException ignored) {

                    }
                }
            }
        }
        return out;
    }

    public void resetAlternatives(Collection<TokenString> alternatives) {
        this.alternatives.clear();
        this.alternatives.addAll(alternatives);
    }

    private boolean exist(TokenString ts) {
        for (TokenString t : alternatives) {
            if (t.equals(ts)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(leftHandSide).append(" := ");
        boolean first = true;
        for (TokenString alt : alternatives) {
            if (!first) {
                out.append(" | ");
            }
            first = false;
            out.append(alt);
        }
        return out.toString();
    }

    public boolean equals(BnfRule br) {
        if (!leftHandSide.getName().equals(br.getLeftHandSide().getName())) {
            return false;
        }
        if (alternatives.size() != br.getAlternatives().size()) {
            return false;
        }
        for (int i = 0; i < alternatives.size(); i++) {
            if (!alternatives.get(i).equals(br.getAlternatives().get(i))) {
                return false;
            }
        }
        return true;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("leftHandSide", leftHandSide.toJSON());
        JSONArray jsonArray = new JSONArray();
        alternatives.forEach(f -> jsonArray.add(f.toJSON()));
        json.put("alternatives", alternatives);
        return json;
    }

}
