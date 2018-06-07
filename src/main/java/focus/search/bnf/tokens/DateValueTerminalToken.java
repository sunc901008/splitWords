package focus.search.bnf.tokens;

import focus.search.base.Constant;

public class DateValueTerminalToken extends TerminalToken {
    public static final String DATE_VALUE = "<date-value>";
    public static final String DATE_VALUE_BNF = "<date-string-value>";

    /**
     * Creates a new non terminal token
     *
     * @param label The token's label
     */
    public DateValueTerminalToken(String label) {
        super(label, Constant.FNDType.DATE_VALUE);
    }

    @Override
    public boolean matches(final Token tok) {
        return true;
    }

    @Override
    public boolean match(final String s) {
        return true;
    }

}
