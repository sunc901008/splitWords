package focus.search.bnf;

import com.alibaba.fastjson.JSONArray;
import focus.search.bnf.tokens.NonTerminalToken;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.TokenString;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/6
 * description:
 */
public class ModelBuild {

    public static void build(FocusParser fp, List<SourceReceived> sources) {
        // add split words
        fp.focusAnalyzer.addTable(sources);

        // add bnf rule
        for (SourceReceived source : sources) {
            for (ColumnReceived col : source.columns) {
                BnfRule br = new BnfRule();
                if (col.columnType.equalsIgnoreCase("measure")) {
                    br.setLeftHandSide(new NonTerminalToken("<int-measure-column>"));
                } else {
                    br.setLeftHandSide(new NonTerminalToken("<string-attribute-column>"));
                }
                TokenString alternative_to_add = new TokenString();
                alternative_to_add.add(new TerminalToken(col.columnDisplayName));
                br.addAlternative(alternative_to_add);
                fp.addRule(br);

                BnfRule br1 = new BnfRule();
                if (col.columnType.equalsIgnoreCase("measure")) {
                    br1.setLeftHandSide(new NonTerminalToken("<table-int-measure-column>"));
                } else {
                    br1.setLeftHandSide(new NonTerminalToken("<table-string-attribute-column>"));
                }
                TokenString alternative_to_add1 = new TokenString();
                alternative_to_add1.add(new TerminalToken(source.sourceName));
                alternative_to_add1.add(new TerminalToken(col.columnDisplayName));
                br1.addAlternative(alternative_to_add1);
                fp.addRule(br1);
            }
        }
    }

    public static List<SourceReceived> test() {
        String test = "[{\"columns\":[{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"SUM\",\"attributeDimension\":0,\"columnDisplayName\":\"age\",\"columnId\":15,\"columnModify\":{\"name\":\"age\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"age\",\"columnType\":\"MEASURE\",\"currencyFormat\":\"\",\"dataType\":\"double\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DONT_INDEX\",\"numFormat\":\"\",\"physicalName\":\"col_p96511\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"NONE\",\"attributeDimension\":0,\"columnDisplayName\":\"creationdate\",\"columnId\":8,\"columnModify\":{\"name\":\"creationdate\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"creationdate\",\"columnType\":\"ATTRIBUTE\",\"currencyFormat\":\"\",\"dataType\":\"timestamp\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DEFAULT\",\"numFormat\":\"\",\"physicalName\":\"col_p1586485005\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"SUM\",\"attributeDimension\":0,\"columnDisplayName\":\"downvotes\",\"columnId\":10,\"columnModify\":{\"name\":\"downvotes\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"downvotes\",\"columnType\":\"MEASURE\",\"currencyFormat\":\"\",\"dataType\":\"double\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DONT_INDEX\",\"numFormat\":\"\",\"physicalName\":\"col_p1321958247\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"NONE\",\"attributeDimension\":0,\"columnDisplayName\":\"emailhash\",\"columnId\":7,\"columnModify\":{\"name\":\"emailhash\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"emailhash\",\"columnType\":\"ATTRIBUTE\",\"currencyFormat\":\"\",\"dataType\":\"string\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DONT_INDEX\",\"numFormat\":\"\",\"physicalName\":\"col_p2120998570\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"SUM\",\"attributeDimension\":0,\"columnDisplayName\":\"id\",\"columnId\":13,\"columnModify\":{\"name\":\"id\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"id\",\"columnType\":\"MEASURE\",\"currencyFormat\":\"\",\"dataType\":\"double\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DONT_INDEX\",\"numFormat\":\"\",\"physicalName\":\"col_p3355\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"NONE\",\"attributeDimension\":0,\"columnDisplayName\":\"lastaccessdate\",\"columnId\":9,\"columnModify\":{\"name\":\"lastaccessdate\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"lastaccessdate\",\"columnType\":\"ATTRIBUTE\",\"currencyFormat\":\"\",\"dataType\":\"timestamp\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DEFAULT\",\"numFormat\":\"\",\"physicalName\":\"col_p1369055784\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"NONE\",\"attributeDimension\":0,\"columnDisplayName\":\"name\",\"columnId\":12,\"columnModify\":{\"name\":\"name\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"name\",\"columnType\":\"ATTRIBUTE\",\"currencyFormat\":\"\",\"dataType\":\"string\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DONT_INDEX\",\"numFormat\":\"\",\"physicalName\":\"col_p1715102285\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"SUM\",\"attributeDimension\":0,\"columnDisplayName\":\"reputation\",\"columnId\":6,\"columnModify\":{\"name\":\"reputation\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"reputation\",\"columnType\":\"MEASURE\",\"currencyFormat\":\"\",\"dataType\":\"double\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DONT_INDEX\",\"numFormat\":\"\",\"physicalName\":\"col_m1292876679\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"SUM\",\"attributeDimension\":0,\"columnDisplayName\":\"upvotes\",\"columnId\":11,\"columnModify\":{\"name\":\"upvotes\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"upvotes\",\"columnType\":\"MEASURE\",\"currencyFormat\":\"\",\"dataType\":\"double\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DONT_INDEX\",\"numFormat\":\"\",\"physicalName\":\"col_m217389810\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"SUM\",\"attributeDimension\":0,\"columnDisplayName\":\"views\",\"columnId\":14,\"columnModify\":{\"name\":\"views\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"views\",\"columnType\":\"MEASURE\",\"currencyFormat\":\"\",\"dataType\":\"double\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DONT_INDEX\",\"numFormat\":\"\",\"physicalName\":\"col_p112204398\",\"priority\":1,\"synonyms\":\"\"},{\"DBName\":\"sctest\",\"additive\":0,\"aggregation\":\"NONE\",\"attributeDimension\":0,\"columnDisplayName\":\"websiteurl\",\"columnId\":5,\"columnModify\":{\"name\":\"websiteurl\",\"samples\":[],\"source\":\"sctest.sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"updated\":\"2018-02-06 15:16:43\"},\"columnName\":\"websiteurl\",\"columnType\":\"ATTRIBUTE\",\"currencyFormat\":\"\",\"dataType\":\"string\",\"dateFormat\":\"\",\"description\":\"\",\"geoType\":\"\",\"hidden\":0,\"indexType\":\"DONT_INDEX\",\"numFormat\":\"\",\"physicalName\":\"col_p1317165812\",\"priority\":1,\"synonyms\":\"\"}],\"parentDB\":\"sctest\",\"physicalName\":\"sql_192_168_0_93_5432_releaseci_p111578632_11m1142892812\",\"sourceName\":\"users\",\"tableId\":2,\"type\":\"table\"}]";
        return JSONArray.parseArray(test, SourceReceived.class);
    }

}
