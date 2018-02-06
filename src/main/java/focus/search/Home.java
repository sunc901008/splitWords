package focus.search;

import com.alibaba.fastjson.JSON;
import focus.search.analyzer.focus.FocusToken;
import focus.search.bnf.*;
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

        FocusParser parser = new FocusParser();
        ModelBuild.build(parser, ModelBuild.test());

        String search = "i";
        List<FocusToken> tokens = parser.focusAnalyzer.test(search, "english");
        System.out.println(JSON.toJSONString(tokens));
        FocusInst focusInst = parser.parse(tokens);
        System.out.println("-------------------");
        System.out.println(JSON.toJSONString(focusInst));

        FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
        if (focusPhrase.isSuggestion()) {
            int sug = 0;
            while (sug < focusPhrase.size()) {
                FocusNode tmpNode = focusPhrase.getNode(sug);
                if (! tmpNode.isTerminal() || !tmpNode.getValue().equalsIgnoreCase(tmpNode.getFt().getWord())) {
                    System.out.println("------------------------");
                    String msg = "输入不完整:\n\t提示:" + tmpNode.getValue() + "\n";
                    System.out.println(msg);
                    return;
                }
                sug++;
            }
        }

    }


}
