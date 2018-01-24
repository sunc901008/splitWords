package focus.search.bnf;


import java.util.ArrayList;
import java.util.List;

/**
 * A node in the parse tree created by the parser.
 *
 * @author Sylvain Hallé
 */
public class ParseNode {
    /**
     * A list of children of this parse node
     */
    private ArrayList<ParseNode> m_children = null;

    /**
     * The grammar token represented by this parse node
     */
    private String m_token = null;

    /**
     * The value (if any) represented by this parse node
     */
    private String m_value = null;

    /**
     * Creates an empty parse node
     */
    public ParseNode() {
        super();
        m_children = new ArrayList<>();
    }

    /**
     * Creates a parse node for a grammar token
     *
     * @param token The token
     */
    public ParseNode(String token) {
        this();
        setToken(token);
    }

    /**
     * Gets the children of this parse node. This method returns a
     * <em>new</em> list instance, and not the internal list the parse
     * node uses to store its children.
     *
     * @return The list of children
     */
    public List<ParseNode> getChildren() {
        if (m_children != null) {
            ArrayList<ParseNode> nodes = new ArrayList<ParseNode>(m_children.size());
            nodes.addAll(m_children);
            return nodes;
        }
        return new ArrayList<ParseNode>(0);
    }

    /**
     * Gets the value of this parse node
     *
     * @return The value
     */
    public String getValue() {
        return m_value;
    }

    /**
     * Gets the token name associated to this parse node
     *
     * @return The token name
     */
    public String getToken() {
        return m_token;
    }

    /**
     * Sets the value for this parse node
     *
     * @param value The value
     */
    public void setValue(final String value) {
        m_value = value;
    }

    /**
     * Sets the token name for this parse node
     *
     * @param token The token name
     */
    public void setToken(final String token) {
        m_token = token;
    }

    /**
     * Adds a child to this parse node
     *
     * @param child A child
     */
    public void addChild(final ParseNode child) {
        m_children.add(child);
    }

    @Override
    public String toString() {
        return toString("");
    }

    /**
     * Produces a string rendition of the contents of this parse node
     *
     * @param indent The indent to add to every line of the output
     * @return The contents of this parse node as a string
     */
    private String toString(String indent) {
        StringBuilder out = new StringBuilder();
        out.append(indent).append(m_token).append("\n");
        String n_indent = indent + " ";
        for (ParseNode n : m_children) {
            out.append(n.toString(n_indent));
        }
        return out.toString();
    }

    /**
     * Returns the size of the parse tree
     *
     * @return The number of nodes
     */
    public int getSize() {
        int size = 1;
        for (ParseNode node : m_children) {
            size += node.getSize();
        }
        return size;
    }

}