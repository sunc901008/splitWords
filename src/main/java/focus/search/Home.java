package focus.search;

import com.alibaba.fastjson.JSON;
import focus.search.bnf.BnfRule;
import focus.search.bnf.FocusParser;
import focus.search.bnf.exception.InvalidRuleException;

import java.io.IOException;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class Home {

    public static void main(String[] args) throws IOException, InvalidRuleException {

        DefaultModel.defaultRules();
        String question = "sort sort";
        FocusParser.parse(question);

//        List<BnfRule> rules = FocusParser.parseRules(question);
//        System.out.println(JSON.toJSONString(rules));

    }

}
