package focus.search.bnf.tokens;

import focus.search.meta.Column;

public class TerminalToken extends Token {
    private static final long serialVersionUID = -5734730721366371245L;
    private String type;
    private Column column;

    /**
     * Creates a new terminal tokens with a label
     */
    public TerminalToken(final String label, String type) {
        super(label);
        this.type = type;
    }

    public TerminalToken(final String label, String type, Column column) {
        super(label);
        this.type = type;
        this.column = column;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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