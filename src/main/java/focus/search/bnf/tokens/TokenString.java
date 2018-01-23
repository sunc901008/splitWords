package focus.search.bnf.tokens;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class TokenString extends LinkedList<Token> {

    /**
     * Creates a new empty token string
     */
    public TokenString() {
        super();
    }

    /**
     * Gets the set of all terminal tokens that appear in this string
     *
     * @return The set of tokens
     */
    public Set<TerminalToken> getTerminalTokens() {
        Set<TerminalToken> out = new HashSet<>();
        for (Token t : this) {
            if (t instanceof TerminalToken)
                out.add((TerminalToken) t);
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TokenString)) {
            return false;
        }
        TokenString rt = (TokenString) o;
        if (rt.size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            Token t1 = get(i);
            Token t2 = rt.get(i);
            if (!t1.equals(t2)) {
                return false;
            }
        }
        return true;
    }

    public final TokenString getCopy() {
        TokenString out = new TokenString();
        out.addAll(this);
        return out;
    }

}
