package focus.search.bnf.tokens;

public class TerminalToken extends Token {
    private static final long serialVersionUID = -5734730721366371245L;

    /**
     * Creates a new empty terminal token
     */
    TerminalToken() {
        super("");
    }

    /**
     * Creates a new terminal token with a label
     */
    public TerminalToken(final String label) {
        super(label);
    }

    @Override
    public boolean matches(final Token tok) {
        return tok != null && getName().compareToIgnoreCase(tok.getName()) == 0;
    }

    @Override
    public boolean match(final String s) {
        return getName().equalsIgnoreCase(s);
    }

}