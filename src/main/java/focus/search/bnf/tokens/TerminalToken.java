package focus.search.bnf.tokens;

import focus.search.meta.Column;

public class TerminalToken extends Token {
    private static final long serialVersionUID = -5734730721366371245L;

    private Column column;

    /**
     * Creates a new empty terminal token
     */
    TerminalToken() {
        super("");
    }

    /**
     * Creates a new terminal token with a label
     */

    public TerminalToken(final String label) {
        super(label);
    }

    public TerminalToken(final Column column) {
        super(column.getColumnDisplayName());
        this.column = column;
    }

    public TerminalToken(final String label, final Column column) {
        super(label);
        this.column = column;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    @Override
    public boolean matches(final Token tok) {
        return tok != null && getName().compareToIgnoreCase(tok.getName()) == 0;
    }

    @Override
    public boolean match(final String s) {
        return getName().toLowerCase().startsWith(s.toLowerCase());
    }

}