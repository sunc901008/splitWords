package focus.search.bnf.tokens;

import focus.search.base.Common;
import focus.search.base.Constant;

public class NumberTerminalToken extends TerminalToken {
    public static final String DOUBLE = "<double>";

    /**
     * Creates a new non terminal tokens
     *
     * @param label The tokens's label
     */
    public NumberTerminalToken(String label) {
        super(label, Constant.FNDType.DOUBLE);
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
        return Common.doubleCheck(s);
    }

}
