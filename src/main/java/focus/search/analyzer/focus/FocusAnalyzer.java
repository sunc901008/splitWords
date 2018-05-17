package focus.search.analyzer.focus;

import focus.search.analyzer.dic.Dictionary;
import focus.search.analyzer.lucene.IKAnalyzer;
import focus.search.meta.Formula;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FocusAnalyzer implements Serializable {

    private IKAnalyzer analyzer;

    private void init() {
        analyzer = new IKAnalyzer();
        Dictionary.addWords(FocusKWDict.allKeywords());
    }

    /**
     * for debug test
     */
    public void testInit(FocusKWDict fk) {
        init();
        Dictionary.addWord(fk);
    }

    // 添加表名列名到分词中
    public void addTable(List<SourceReceived> sources) {
        if (analyzer == null)
            init();
        Dictionary.addWords(makeTableDict(sources));
    }

    // 添加公式名到分词中
    public void addFormulas(List<Formula> formulas) {
        if (analyzer == null)
            init();
        Dictionary.addWords(makeFormulaDict(formulas));
    }

    // 从分词中删除公式名
    public void removeFormulas(List<String> formulaNames) {
        if (analyzer == null)
            init();
        Dictionary.removeWords(formulaNames);
    }

    public void reset() {
        Dictionary.reset();
        Dictionary.addWords(FocusKWDict.allKeywords());
    }

    public List<FocusToken> test(String str, String language) throws IOException {

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
            tokens.add(token);
        }
        // 关闭TokenStream（关闭StringReader）
        ts.close();

        return MergeToken.mergeUserInput(tokens, str);
//        return tokens;
    }

    private List<FocusKWDict> makeTableDict(List<SourceReceived> sources) {
        List<FocusKWDict> list = new ArrayList<>();
        for (SourceReceived source : sources) {
            list.add(new FocusKWDict(source.sourceName, "sourceName"));
            for (ColumnReceived col : source.columns) {
                list.add(new FocusKWDict(col.columnDisplayName, "columnName"));
            }
        }
        return list;
    }

    private List<FocusKWDict> makeFormulaDict(List<Formula> formulas) {
        List<FocusKWDict> list = new ArrayList<>();
        for (Formula formula : formulas) {
            list.add(new FocusKWDict(formula.getName(), "formulaName"));
        }
        return list;
    }

}
