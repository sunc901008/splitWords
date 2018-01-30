package focus.search;

import focus.search.bnf.FocusParser;
import focus.search.bnf.exception.InvalidRuleException;

import java.io.IOException;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class Home {

    public static void main(String[] args) throws IOException, InvalidRuleException {

        DefaultModel.defaultRules();
        String question = "id)";
        FocusParser.parse(question);

    }

}
