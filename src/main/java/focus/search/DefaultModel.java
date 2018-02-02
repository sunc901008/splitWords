package focus.search;

import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.bnf.BnfRule;
import focus.search.bnf.FocusParser;
import focus.search.bnf.tokens.NonTerminalToken;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.TokenString;
import focus.search.meta.Column;
import focus.search.meta.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
public final class DefaultModel {

    static void defaultRules() {
        List<Source> sources = sources();
        // add split words
        FocusAnalyzer.addTable(sources);

        // add bnf rule
        for (Source source : sources) {
            for (Column col : source.getColumns()) {
                BnfRule br = new BnfRule();
                if (col.getColumnType().equalsIgnoreCase("measure")) {
                    br.setLeftHandSide(new NonTerminalToken("<int-measure-column>"));
                } else {
                    br.setLeftHandSide(new NonTerminalToken("<string-attribute-column>"));
                }
                TokenString alternative_to_add = new TokenString();
                alternative_to_add.add(new TerminalToken(col));
                br.addAlternative(alternative_to_add);
                FocusParser.addRule(br);

                BnfRule br1 = new BnfRule();
                if (col.getColumnType().equalsIgnoreCase("measure")) {
                    br1.setLeftHandSide(new NonTerminalToken("<table-int-measure-column>"));
                } else {
                    br1.setLeftHandSide(new NonTerminalToken("<table-string-attribute-column>"));
                }
                TokenString alternative_to_add1 = new TokenString();
                alternative_to_add1.add(new TerminalToken(source.getSourceName()));
                alternative_to_add1.add(new TerminalToken(col));
                br1.addAlternative(alternative_to_add1);
                FocusParser.addRule(br1);
            }
        }
    }

    public static List<Source> sources() {
        // source info
        List<Source> sources = new ArrayList<>();
        Source source = new Source();
        source.setTableId(1);
        source.setType("table");
        source.setSourceName("users");
        source.addColumn(new Column(12, "views", "int", "measure"));
        source.addColumn(new Column(15, "id", "int", "measure"));
        source.addColumn(new Column(11, "displayname", "string", "attribute"));
        sources.add(source);
        return sources;
    }

}
