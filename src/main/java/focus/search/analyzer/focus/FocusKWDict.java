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

    public FocusKWDict(String word, String type) {
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
        List<String> added = new ArrayList<>();
        for (TerminalToken token : Base.englishParser.getTerminalTokens()) {
            String value = token.getName().toLowerCase();
            if (!added.contains(value)) {
                dictionaries.add(new FocusKWDict(token.getName().toLowerCase(), Constant.FNDType.KEYWORD));
                added.add(value);
            }
        }
        for (TerminalToken token : Base.chineseParser.getTerminalTokens()) {
            String value = token.getName().toLowerCase();
            if (!added.contains(value)) {
                dictionaries.add(new FocusKWDict(token.getName().toLowerCase(), Constant.FNDType.KEYWORD));
                added.add(value);
            }
        }
        return dictionaries;
    }

}
