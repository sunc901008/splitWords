package focus.search.bnf.exception;

/**
 * creator: sunc
 * date: 2018/1/23
 * description:
 */
public class InvalidGrammarException extends Exception {
    /**
     * Dummy UID
     */
    private static final long serialVersionUID = 1L;

    public InvalidGrammarException(final String message) {
        super(message);
    }

    public InvalidGrammarException(Throwable t) {
        super(t);
    }
}
