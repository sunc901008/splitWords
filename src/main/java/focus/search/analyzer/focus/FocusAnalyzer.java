package focus.search.analyzer.focus;

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
            String amb = e.getMessage();
            logger.info("split exception:" + amb);
            Common.info("split exception:" + amb);
            try {
                AmbiguitiesException ae = AmbiguitiesException.recover(amb);
                assert ae != null;
                throw ae;
            } catch (Exception e1) {
                logger.info(Common.printStacktrace(e1));
                throw e1;
            }

        }
        // 关闭TokenStream（关闭StringReader）
        ts.close();

        tokens = mergeQuoteValue(tokens, str);
        return mergeMinus(tokens);
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

    // 合并引号包含的列中值
    private static List<FocusToken> mergeQuoteValue(List<FocusToken> tokens, String search) {
        List<FocusToken> merge = new ArrayList<>();
        int start = 0;
        int end = 0;
        quoteNode qn = new quoteNode();
        while (!tokens.isEmpty()) {
            FocusToken tmp = tokens.remove(0);
            if (qn.hasBegin) {
                String word = tmp.getWord();
                if (couple(qn, word)) {
                    qn.hasEnd = true;
                    qn.hasBegin = false;
                    end = tmp.getStart();
                    merge.add(new FocusToken(search.substring(start, end), Constant.FNDType.COLUMN_VALUE, start, end));
                    merge.add(tmp);
                }
            } else {
                merge.add(tmp);
                if (Constant.START_QUOTES.contains(tmp.getWord())) {
                    qn.hasBegin = true;
                    qn.hasEnd = false;
                    qn.value = tmp.getWord();
                    start = tmp.getEnd();
                }
            }
        }
        if (qn.hasBegin && !qn.hasEnd) {
            end = search.length();
            if (start < end)
                merge.add(new FocusToken(search.substring(start), Constant.FNDType.COLUMN_VALUE, start, end));
        }
        return merge;
    }

    // 合并负号
    private static List<FocusToken> mergeMinus(List<FocusToken> tokens) {
        List<FocusToken> merge = new ArrayList<>();
        if (tokens.size() < 2) {
            return tokens;
        } else if (tokens.size() == 2) {
            FocusToken first = tokens.get(0);
            FocusToken second = tokens.get(1);
            if (Constant.MINUS.equals(first.getWord())
                    && Common.isNumber(second.getType())) {
                merge.add(new FocusToken(first.getWord() + second.getWord(), second.getType(), first.getStart(), second.getEnd()));
                return merge;
            }
            return tokens;
        }
        FocusToken first = tokens.remove(0);
        int loop = tokens.size();
        merge.add(first);
        while (loop > 0) {
            FocusToken current = tokens.remove(0);
            if (tokens.isEmpty()) {
                merge.add(current);
                break;
            }
            FocusToken next = tokens.get(0);
            if (!Common.isNumber(first.getType())
                    && Constant.MINUS.equals(current.getWord())
                    && Common.isNumber(next.getType())) {
                merge.add(new FocusToken(current.getWord() + next.getWord(), next.getType(), current.getStart(), next.getEnd()));
                tokens.remove(0);
                loop--;
                first = next;
            } else {
                merge.add(current);
                first = current;
            }
            loop--;
        }
        return merge;
    }

    /**
     * @param qn   引号节点，用来成对匹配
     * @param word 当前节点值
     * @return 判断当前节点是否可以和之前的引号形成一组(中文引号匹配问题)
     */
    private static boolean couple(quoteNode qn, String word) {
        return Constant.END_QUOTES.contains(word)
                && (word.equals(qn.value) || ("“".equals(qn.value)) && "”".equals(word));
    }

    private static class quoteNode {
        boolean hasBegin = false;
        boolean hasEnd = false;
        String value;
    }

}
