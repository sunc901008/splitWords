package focus.search.bnf.tokens;

import java.io.Serializable;

public abstract class Token implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The token's name
     */
    private String m_name;

    /**
     * Creates a token with a name
     */
    Token(String name) {
        if (name != null) {
            m_name = name;
        }
    }

    /**
     * Gets the name of this token
     *
     * @return The name
     */
    public String getName() {
        return m_name;
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || !(o instanceof Token)) && ((Token) o).getName().compareTo(m_name) == 0;
    }

    public abstract boolean matches(final Token tok);

    public abstract boolean match(final String s);

}
