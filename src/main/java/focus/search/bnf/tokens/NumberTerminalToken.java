package focus.search.bnf.tokens;

public class NumberTerminalToken extends TerminalToken {
    public static final String NUMBER = "<number>";

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


    @Override
    public boolean match(final String s) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}