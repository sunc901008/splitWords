package focus.search;

import com.alibaba.fastjson.JSON;
import focus.search.analyzer.focus.FocusToken;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class Home {

    public static void main(String[] args) throws IOException, InvalidRuleException {

        FocusParser parser = new FocusParser();
        ModelBuild.build(parser, ModelBuild.test());

        String search = "users";
        List<FocusToken> tokens = parser.focusAnalyzer.test(search, "english");
        System.out.println(JSON.toJSONString(tokens));
        FocusInst focusInst = parser.parse(tokens);
        System.out.println("-------------------");
        System.out.println(focusInst.toJSON());

        List<BnfRule> rules = parser.parseRules("users");

        System.out.println(JSON.toJSONString(rules));

        /*
        int position = focusInst.position;
        if (position < 0) {
            FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
            if (focusPhrase.isSuggestion()) {
                String msg = "提示:\n\t" + JSON.toJSONString(sug(tokens.size() - 1, focusInst)) + "\n";
                System.out.println(msg);
            } else {
                System.out.println("build instruction!");
            }
        } else {
            int strPosition = tokens.get(position).getStart();
            String msg = "错误:\n\t" + "位置: " + strPosition + "\t错误: " + search.substring(strPosition) + "\n";
            System.out.println(msg);
            msg = "提示:\n\t" + JSON.toJSONString(sug(position, focusInst)) + "\n";
            System.out.println(msg);
        }
        */

    }

    private static Set<String> sug(int position, FocusInst focusInst) {
        Set<String> suggestions = new HashSet<>();
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            if (fp.isSuggestion()) {
                suggestions.add(fp.getNode(position).getValue());
            } else {
                position = position - fp.size();
            }
        }
        return suggestions;
    }

}
