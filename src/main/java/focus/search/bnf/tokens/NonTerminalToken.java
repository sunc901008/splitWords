package focus.search.bnf.tokens;

public class NonTerminalToken extends Token {
    private static final long serialVersionUID = -6214246017840941018L;

    /**
     * The left-hand side symbol used to mark a non-terminal tokens
     * in a grammar rule
     */
    public static final transient String s_leftSymbol = "<";

    /**
     * The right-hand side symbol used to mark a non-terminal tokens
     * in a grammar rule
     */
    public static final transient String s_rightSymbol = ">";

    public NonTerminalToken(String s) {
        super(s);
    }

    @Override
    public boolean matches(final Token tok) {
        return false;
    }

    @Override
    public boolean match(String s) {
        return false;
    }

}
