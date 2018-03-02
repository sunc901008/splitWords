package focus.search.bnf.tokens;

public class StringTerminalToken extends TerminalToken {
    private static final long serialVersionUID = -8815329986927875183L;

    public static transient final String STRING_SYMBOL = "'";

    /**
     * Creates a new empty string terminal token
     */
    protected StringTerminalToken() {
        super("", "");
    }

    /**
     * Creates a new string terminal token
     *
     * @param label The string this token should match
     */
    public StringTerminalToken(String label) {
        super(label, "");
    }

    @Override
    public boolean matches(final Token tok) {
        // Anything matches a string
        return tok != null;
    }

}
