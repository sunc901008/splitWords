package focus.search.bnf.tokens;

public class IntegerTerminalToken extends TerminalToken {

    public static final String INTEGER = "<integer>";

    /**
     * Creates a new non terminal token
     *
     * @param label The token's label
     */
    public IntegerTerminalToken(String label) {
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
            Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    @Override
    public boolean match(final String s) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
