package focus.search.bnf;

import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.tokens.NonTerminalToken;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.TokenString;
import focus.search.controller.common.FormulaAnalysis;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;

import java.util.*;

/**
 * creator: sunc
 * date: 2018/2/6
 * description:
 */
public class ModelBuild {

    public static void buildTable(FocusParser fp, List<SourceReceived> sources) {
        // add split words
        fp.focusAnalyzer.addTable(sources);

        // add bnf rule
        for (SourceReceived source : sources) {
            for (ColumnReceived col : source.columns) {
                BnfRule br = new BnfRule();
                BnfRule br1 = new BnfRule();
                if (col.dataType.equalsIgnoreCase(Constant.DataType.INT)) {
                    br.setLeftHandSide(new NonTerminalToken("<int-column>"));
                    br1.setLeftHandSide(new NonTerminalToken("<table-int-column>"));
                } else if (col.dataType.equalsIgnoreCase(Constant.DataType.DOUBLE)) {
                    br.setLeftHandSide(new NonTerminalToken("<double-column>"));
                    br1.setLeftHandSide(new NonTerminalToken("<table-double-column>"));
                } else if (col.dataType.equalsIgnoreCase(Constant.DataType.TIMESTAMP)) {
                    br.setLeftHandSide(new NonTerminalToken("<timestamp-column>"));
                    br1.setLeftHandSide(new NonTerminalToken("<table-timestamp-column>"));
                } else if (col.dataType.equalsIgnoreCase(Constant.DataType.BOOLEAN)) {
                    br.setLeftHandSide(new NonTerminalToken("<boolean-column>"));
                    br1.setLeftHandSide(new NonTerminalToken("<table-boolean-column>"));
                } else {
                    br.setLeftHandSide(new NonTerminalToken("<string-column>"));
                    br1.setLeftHandSide(new NonTerminalToken("<table-string-column>"));
                }
                TokenString alternative_to_add = new TokenString();

                Column column = col.transfer();
                column.setTableId(source.tableId);
                column.setSourceName(source.sourceName);
                column.setTbPhysicalName(source.physicalName);
                column.setDbName(source.parentDB);

                alternative_to_add.add(new TerminalToken(col.columnDisplayName, Constant.FNDType.COLUMN, column));
                br.addAlternative(alternative_to_add);
                fp.addRule(br);

                TokenString alternative_to_add1 = new TokenString();
                alternative_to_add1.add(new TerminalToken(source.sourceName, Constant.FNDType.TABLE));
                alternative_to_add1.add(new TerminalToken(col.columnDisplayName, Constant.FNDType.COLUMN, column));
                br1.addAlternative(alternative_to_add1);
                fp.addRule(br1);
            }
        }
    }

    public static final Map<String, String> tokenNames = new HashMap<>();

    static {
        tokenNames.put(FormulaAnalysis.BOOLEAN, "<bool-formula-column>");
        tokenNames.put(FormulaAnalysis.NUMERIC, "<number-formula-column>");
        tokenNames.put(FormulaAnalysis.STRING, "<string-formula-column>");
        tokenNames.put(FormulaAnalysis.TIMESTAMP, "<date-formula-column>");
    }

    public static void buildFormulas(FocusParser fp, List<Formula> formulas) {
        // add split words
        fp.focusAnalyzer.addFormulas(formulas);

        // add bnf rule
        for (Formula formula : formulas) {
            BnfRule br = new BnfRule();
            String tokenName = tokenNames.get(formula.getDataType());
            if (Common.isEmpty(tokenName)) {
                continue;
            }
            br.setLeftHandSide(new NonTerminalToken(tokenName));
            TokenString alternative_to_add = new TokenString();
            alternative_to_add.add(new TerminalToken(formula.getName(), Constant.FNDType.FORMULA));
            br.addAlternative(alternative_to_add);
            fp.addRule(br);
        }
    }

    public static void deleteFormulas(FocusParser fp, List<String> formulas) {
        // add split words
        fp.focusAnalyzer.removeFormulas(formulas);

        // delete bnf rule
        Collection<String> values = tokenNames.values();
        for (String value : values) {
            BnfRule br = fp.getRule(value);
            if (br != null) {
                BnfRule brNew = new BnfRule();
                brNew.setLeftHandSide(new NonTerminalToken(value));
                List<TokenString> tokenStrings = new ArrayList<>();
                for (TokenString ts : br.getAlternatives()) {
                    if (!formulas.contains(ts.getFirst().getName())) {
                        tokenStrings.add(ts);
                    }
                }
                brNew.resetAlternatives(tokenStrings);
                fp.resetRule(brNew);
            }
        }
    }

}
