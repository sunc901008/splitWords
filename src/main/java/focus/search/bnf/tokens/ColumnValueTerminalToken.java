package focus.search.bnf.tokens;

import focus.search.base.Constant;

public class ColumnValueTerminalToken extends TerminalToken {
    public static final String COLUMN_VALUE = "<value>";
    public static final String COLUMN_VALUE_BNF = "<column-value>";

    /**
     * Creates a new non terminal tokens
     *
     * @param label The tokens's label
     */
    public ColumnValueTerminalToken(String label) {
        super(label, Constant.FNDType.COLUMN_VALUE);
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
