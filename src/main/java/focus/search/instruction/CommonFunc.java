package focus.search.instruction;

import focus.search.bnf.FocusParser;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.meta.Column;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
public class CommonFunc {

    static Column getCol(String colName) {
        List<TerminalToken> tokens = FocusParser.getTerminalTokens();
        for (TerminalToken ter : tokens) {
            if (ter.getName().equalsIgnoreCase(colName) && ter.getColumn() != null) {
                return ter.getColumn();
            }
        }
        return null;
    }

}
