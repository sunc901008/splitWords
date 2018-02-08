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
    private String attr;

    public FocusKWDict(String word, String type, String attr) {
        this.word = word;
        this.type = type;
        this.attr = attr;
    }

    static {
        dictionaries.add(new FocusKWDict("的最大值", "keyword", "keyword"));
        dictionaries.add(new FocusKWDict("最大值", "keyword", "keyword"));
        dictionaries.add(new FocusKWDict("的最小值", "keyword", "keyword"));
        dictionaries.add(new FocusKWDict("最小值", "keyword", "keyword"));
        dictionaries.add(new FocusKWDict("前", "keyword", "keyword"));
        dictionaries.add(new FocusKWDict("sort", "keyword", "keyword"));
        dictionaries.add(new FocusKWDict("by", "keyword", "keyword"));
        dictionaries.add(new FocusKWDict("top", "keyword", "keyword"));
    }

    public String getWord() {
        return word;
    }

    public String getType() {
        return type;
    }

    public String getAttr() {
        return attr;
    }

    public static List<String> getAllKeywords() {
        List<String> list = new ArrayList<>();
        dictionaries.forEach(dict -> list.add(dict.getWord()));
        return list;
    }

}
