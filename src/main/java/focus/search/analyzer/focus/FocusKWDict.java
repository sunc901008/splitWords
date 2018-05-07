package focus.search.analyzer.focus;

import focus.search.base.Constant;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.controller.common.Base;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/17
 * description:
 */
public class FocusKWDict {

    private String word;
    private String type;

    FocusKWDict(String word, String type) {
        this.word = word;
        this.type = type;
    }

    public String getWord() {
        return word;
    }

    public String getType() {
        return type;
    }

    static List<FocusKWDict> allKeywords() {
        List<FocusKWDict> dictionaries = new ArrayList<>();
        for (TerminalToken token : Base.englishParser.getTerminalTokens()) {
            dictionaries.add(new FocusKWDict(token.getName().toLowerCase(), Constant.FNDType.KEYWORD));
        }
        for (TerminalToken token : Base.chineseParser.getTerminalTokens()) {
            dictionaries.add(new FocusKWDict(token.getName().toLowerCase(), Constant.FNDType.KEYWORD));
        }
        return dictionaries;
    }

}
