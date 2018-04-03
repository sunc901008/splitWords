package focus.search.bnf.tokens;

import focus.search.base.Common;
import focus.search.base.Constant;

public class ColumnValueTerminalToken extends TerminalToken {
    public static final String COLUMNVALUE = "<value>";

    /**
     * Creates a new non terminal token
     *
     * @param label The token's label
     */
    public ColumnValueTerminalToken(String label) {
        super(label, Constant.FNDType.COLUMNVALUE);
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
