package focus.search.analyzer.focus;

import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.dic.Dictionary;
import focus.search.analyzer.lucene.IKAnalyzer;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.meta.Formula;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.exception.AmbiguitiesException;
import org.apache.log4j.Logger;
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
    private static final Logger logger = Logger.getLogger(FocusAnalyzer.class);

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

    public void testInit(List<FocusKWDict> fks) {
        init();
        Dictionary.addWords(fks);
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

    public List<FocusToken> test(String str, String language) throws IOException, AmbiguitiesException {
        logger.info("start split question.question:" + str + " . language:" + language);
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
        try {
            while (ts.incrementToken()) {
                FocusToken token = new FocusToken(term.toString(), type.type(), offset.startOffset(), offset.endOffset());
                tokens.add(token);
            }
        } catch (IOException e) {
            logger.info("split exception.");
            logger.info(e.getMessage());
            try {
                AmbiguitiesException ae = JSONObject.parseObject(e.getMessage(), AmbiguitiesException.class);
                assert ae != null;
                throw ae;
            } catch (Exception e1) {
                logger.info(Common.printStacktrace(e1));
                throw e1;
            }

        }
        // 关闭TokenStream（关闭StringReader）
        ts.close();

        return mergeUserInput(tokens, str);
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

    private static List<FocusToken> mergeUserInput(List<FocusToken> tokens, String search) {
        List<FocusToken> merge = new ArrayList<>();
        int start = 0;
        int end = 0;
        boolean hasBegin = false;
        boolean hasEnd = false;
        while (!tokens.isEmpty()) {
            FocusToken tmp = tokens.remove(0);
            if (hasBegin) {
                if ("TYPE_QUOTE".equals(tmp.getType())) {
                    hasEnd = true;
                    hasBegin = false;
                    end = tmp.getStart();
                    merge.add(new FocusToken(search.substring(start, end), Constant.FNDType.COLUMN_VALUE, start, end));
                    merge.add(tmp);
                }
            } else {
                merge.add(tmp);
                if ("TYPE_QUOTE".equals(tmp.getType())) {
                    hasBegin = true;
                    hasEnd = false;
                    start = tmp.getEnd();
                }
            }
        }
        if (!hasEnd) {
            end = search.length();
            merge.add(new FocusToken(search.substring(start), Constant.FNDType.COLUMN_VALUE, start, end));
        }
        return merge;
    }

}
