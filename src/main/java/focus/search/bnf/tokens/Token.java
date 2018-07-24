package focus.search.bnf.tokens;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public abstract class Token implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The tokens's name
     */
    private String name;

    public Token() {

    }

    /**
     * Creates a tokens with a name
     */
    public Token(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * Gets the name of this tokens
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return !(o == null || !(o instanceof Token)) && ((Token) o).getName().compareTo(name) == 0;
    }

    public abstract boolean matches(final Token tok);

    public abstract boolean match(final String s);

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        return json;
    }

}
