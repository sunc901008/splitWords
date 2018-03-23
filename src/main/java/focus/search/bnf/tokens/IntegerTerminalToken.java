package focus.search.bnf.tokens;

import focus.search.base.Common;
import focus.search.base.Constant;

public class IntegerTerminalToken extends TerminalToken {

    public static final String INTEGER = "<integer>";

    /**
     * Creates a new non terminal token
     *
     * @param label The token's label
     */
    public IntegerTerminalToken(String label) {
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
            Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean match(final String s) {
        return Common.intCheck(s);
    }

}
