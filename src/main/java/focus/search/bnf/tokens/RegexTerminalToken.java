package focus.search.bnf.tokens;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Terminal symbol in the grammar defined by a regular expression.
 *
 * @author Sylvain Hallé
 */
public class RegexTerminalToken extends TerminalToken {
    /**
     * Dummy UID
     */
    private static final long serialVersionUID = -2430670680001437707L;

    /**
     * The pattern used to perform the matching
     */
    private transient Pattern m_pattern;

    /**
     * Creates a new terminal token
     *
     * @param label The regular expression that matches this token
     */
    public RegexTerminalToken(final String label) {
        super(label);
        m_pattern = Pattern.compile(label);
    }

    @Override
    public boolean matches(final Token tok) {
        String contents = tok.getName();
        Matcher matcher = m_pattern.matcher(contents);
        return matcher.matches();
    }

    @Override
    public boolean match(final String s) {
        Matcher matcher = m_pattern.matcher(s);
        return matcher.find();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof RegexTerminalToken)) {
            return false;
        }
        RegexTerminalToken rt = (RegexTerminalToken) o;
        return m_pattern.toString().equals(rt.m_pattern.toString());
    }

    public List<String> getCaptureBlocks(final String s) {
        List<String> out = new LinkedList<>();
        Matcher matcher = m_pattern.matcher(s);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group_match = matcher.group(i);
                out.add(group_match);
            }
        }
        return out;
    }
}
