package focus.search.bnf.tokens;

import focus.search.base.Constant;

public class NumberTerminalToken extends TerminalToken {
    public static final String NUMBER = "<number>";

    /**
     * Creates a new non terminal token
     *
     * @param label The token's label
     */
    public NumberTerminalToken(String label) {
        super(label, Constant.FNDType.INTEGER);
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
        if (!s.contains(".")) {
            return false;
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
