package focus.search.analyzer.dic;

import focus.search.analyzer.focus.FocusKWDict;

import java.util.ArrayList;
import java.util.List;

/**
 * 词典管理类,单子模式
 */
public class Dictionary {

    /*
     * 词典单子实例
     */
    private static Dictionary singleton;

    private static final List<String> allWords = new ArrayList<>();

    /*
     * 主词典对象
     */
    private DictSegment _MainDict = new DictSegment((char) 0);

    private Dictionary() {
    }

    /**
     * 词典初始化
     * 由于IK Analyzer的词典采用Dictionary类的静态方法进行词典初始化
     * 只有当Dictionary类被实际调用时，才会开始载入词典，
     * 这将延长首次分词操作的时间
     * 该方法提供了一个在应用加载阶段就初始化字典的手段
     */
    public static void initial() {
        if (singleton == null) {
            synchronized (Dictionary.class) {
                if (singleton == null) {
                    singleton = new Dictionary();
                }
            }
        }
    }

    public static void reset() {
        if (singleton != null) {
            singleton._MainDict.reset();
            allWords.clear();
        }
    }

    /**
     * 获取词典单子实例
     *
     * @return Dictionary 单例对象
     */
    public static Dictionary getSingleton() {
        if (singleton == null) {
            throw new IllegalStateException("词典尚未初始化，请先调用initial方法");
        }
        return singleton;
    }

    /**
     * 批量加载新词条
     */
    public static void addWords(List<FocusKWDict> words) {
        if (words != null) {
            for (FocusKWDict word : words) {
                addWord(word);
            }
        }
    }

    /**
     * 批量屏蔽新词条
     */
    public static void removeWords(List<String> words) {
        if (words != null) {
            for (String word : words) {
                deleteWord(word);
            }
        }
    }

    public static void addWord(FocusKWDict word) {
        String key = word.getWord().trim();
        singleton._MainDict.fillSegment(key.toLowerCase().toCharArray(), word.getType());
        allWords.add(key);
    }

    private static void deleteWord(String word) {
        word = word.trim();
        singleton._MainDict.removeSegment(word.trim().toLowerCase().toCharArray());
        allWords.remove(word);
    }

    /**
     * 检索匹配主词典
     */
    public Hit matchInMainDict(char[] charArray, int begin, int length) {
        return singleton._MainDict.match(charArray, begin, length);
    }

    /**
     * 从已匹配的Hit中直接取出DictSegment，继续向下匹配
     */
    public Hit matchWithHit(char[] charArray, int currentIndex, Hit matchedHit) {
        DictSegment ds = matchedHit.getMatchedDictSegment();
        return ds.match(charArray, currentIndex, 1, matchedHit);
    }

    public static List<String> allWords() {
        return allWords;
    }

}
