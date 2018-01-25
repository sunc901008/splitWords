package focus.search.analyzer.focus;

import focus.search.analyzer.dic.Dictionary;
import focus.search.analyzer.lucene.IKAnalyzer;
import focus.search.meta.Column;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FocusAnalyzer {

    private static IKAnalyzer analyzer;

    public static void init() {
        analyzer = new IKAnalyzer();
        Dictionary.addWords(FocusKWDict.dictionaries);
    }

    public static void addTable(List<Column> columns) {
        if (analyzer == null)
            init();
        Dictionary.addWords(makeDict(columns));
    }

    public static void reset() {
        Dictionary.reset();
        Dictionary.addWords(FocusKWDict.dictionaries);
    }

    public static List<FocusToken> test(String str, String language) throws IOException {

        if (analyzer == null) {
            init();
        }

        TokenStream ts = analyzer.tokenStream("focus", new StringReader(str));
        analyzer.loadSegmenters(language);
        // 获取词元位置属性
        OffsetAttribute offset = ts.addAttribute(OffsetAttribute.class);
        // 获取词元文本属性
        CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
        // 获取词元文本属性
        TypeAttribute type = ts.addAttribute(TypeAttribute.class);

        // 重置TokenStream（重置StringReader）
        ts.reset();
        // 迭代获取分词结果
        List<FocusToken> tokens = new LinkedList<>();
        while (ts.incrementToken()) {
            FocusToken token = new FocusToken(term.toString(), type.type(), offset.startOffset(), offset.endOffset());
            analyzer.getSuggestions().forEach(token::addSuggestions);
            analyzer.getAmbiguity().forEach(token::addAmbiguities);
            tokens.add(token);
        }
        // 关闭TokenStream（关闭StringReader）
        ts.close();
        return tokens;
    }

    private static List<FocusKWDict> makeDict(List<Column> columns) {
        List<FocusKWDict> list = new ArrayList<>();
        for (Column col : columns) {
            FocusKWDict dict = new FocusKWDict(col.getName(), "columnName", col.getTblName());
            list.add(dict);
        }
        return list;
    }

}
