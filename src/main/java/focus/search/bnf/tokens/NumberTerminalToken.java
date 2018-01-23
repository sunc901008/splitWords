package focus.search.bnf.tokens;

public class NumberTerminalToken extends TerminalToken {
    private static final long serialVersionUID = -5149336827915614205L;

    /**
     * Creates a new non terminal token
     *
     * @param label The token's label
     */
    public NumberTerminalToken(String label) {
        super(label);
    }

    @Override
    public boolean matches(final Token tok) {
        if (tok == null) {
            return false;
        }
        String val = tok.getName();
        try {
            //noinspection ResultOfMethodCallIgnored
            Float.parseFloat(val);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
