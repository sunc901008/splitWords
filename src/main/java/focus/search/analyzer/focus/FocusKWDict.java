package focus.search.analyzer.focus;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/17
 * description:
 */
public class FocusKWDict {

    public static final List<FocusKWDict> dictionaries = new ArrayList<>();

    private String word;
    private String type;

    public FocusKWDict(String word, String type) {
        this.word = word;
        this.type = type;
    }

    static {
        dictionaries.add(new FocusKWDict("的最大值", "keyword"));
        dictionaries.add(new FocusKWDict("最大值", "keyword"));
        dictionaries.add(new FocusKWDict("的最小值", "keyword"));
        dictionaries.add(new FocusKWDict("最小值", "keyword"));
        dictionaries.add(new FocusKWDict("前", "keyword"));
        dictionaries.add(new FocusKWDict("sort", "keyword"));
        dictionaries.add(new FocusKWDict("by", "keyword"));
        dictionaries.add(new FocusKWDict("top", "keyword"));
    }

    public String getWord() {
        return word;
    }

    public String getType() {
        return type;
    }

    public static List<String> getAllKeywords() {
        List<String> list = new ArrayList<>();
        dictionaries.forEach(dict -> list.add(dict.getWord()));
        return list;
    }

}
