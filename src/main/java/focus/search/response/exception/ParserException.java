package focus.search.response.exception;

import focus.search.bnf.ParseNode;

/**
 * creator: sunc
 * date: 2018/7/18
 * description:
 */
public class ParserException extends Exception {
    public int index;
    public ParseNode node;
}
