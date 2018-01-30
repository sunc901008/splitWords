package focus.search;

import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.bnf.BnfRule;
import focus.search.bnf.FocusParser;
import focus.search.bnf.tokens.NonTerminalToken;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.TokenString;
import focus.search.meta.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
public final class DefaultModel {

    static void defaultRules() {
        List<Column> columns = columns();
        // add split words
        FocusAnalyzer.addTable(columns);

        // add bnf rule
        for (Column col : columns) {
            BnfRule br = new BnfRule();
            if (col.getColType().equalsIgnoreCase("measure")) {
                br.setLeftHandSide(new NonTerminalToken("<int-measure-column>"));
            } else {
                br.setLeftHandSide(new NonTerminalToken("<string-attribute-column>"));
            }
            TokenString alternative_to_add1 = new TokenString();
            alternative_to_add1.add(new TerminalToken(col));
            br.addAlternative(alternative_to_add1);
            FocusParser.addRule(br);
        }
    }

    public static List<Column> columns() {
        // column info
        List<Column> columns = new ArrayList<>();
        Column col1 = new Column(12, "views", "int", "measure");
        col1.setTblName("users");
        columns.add(col1);
        Column col2 = new Column(15, "id", "int", "measure");
        col2.setTblName("users");
        columns.add(col2);
        Column col3 = new Column(11, "displayname", "string", "attribute");
        col3.setTblName("users");
        columns.add(col3);
        return columns;
    }

}
