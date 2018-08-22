package focus.search.analyzer.focus;

import focus.search.analyzer.core.CharacterUtil;
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
        if (Constant.Language.ENGLISH.equals(language)) {
            return test(str);
        }
        if (analyzer == null) {
            init();
        }

        TokenStream ts = analyzer.tokenStream("focus", new StringReader(str));

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

        tokens = mergeQuoteValue(tokens, str);// 合并列中值
        tokens = mergeMinus(tokens);// 合并负号
        return mergePrefixWord(tokens);
    }

    // 英文分词,除特别的符号之外('(',')',',','"','>'...),默认按照空格分词
    public List<FocusToken> test(String str) throws IOException, AmbiguitiesException {
        List<FocusToken> tokens = new LinkedList<>();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int type = CharacterUtil.identifyCharType(c);
            if (CharacterUtil.QUOTE_CHAR == type || CharacterUtil.COMMA_CHAR == type || CharacterUtil.CHAR_PUNCTUATION == type) {
                if (sb.length() > 0) {
                    FocusToken ft = new FocusToken(sb.toString(), type(sb.toString()), i - sb.toString().length(), i);
                    tokens.add(ft);
                }
                FocusToken ft = new FocusToken(String.valueOf(c), type(type), i, i + 1);
                tokens.add(ft);
                sb.delete(0, sb.length());
            } else if (CharacterUtil.CHAR_USELESS == type) {
                if (sb.length() > 0) {
                    FocusToken ft = new FocusToken(sb.toString(), type(sb.toString()), i - sb.toString().length(), i);
                    tokens.add(ft);
                }
                sb.delete(0, sb.length());
            } else {
                sb.append(c);
            }
        }

        if (sb.length() > 0) {
            FocusToken ft = new FocusToken(sb.toString(), type(sb.toString()), str.length() - sb.toString().length(), str.length());
            tokens.add(ft);
        }

        tokens = mergeQuoteValue(tokens, str);
        tokens = mergeMinus(tokens);
        return mergePrefixWord(tokens);
    }

    private String type(String str) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(str);
            return Constant.FNDType.INTEGER;
        } catch (NumberFormatException ignored1) {
            try {
                //noinspection ResultOfMethodCallIgnored
                Double.parseDouble(str);
                return Constant.FNDType.DOUBLE;
            } catch (NumberFormatException ignored2) {
            }
        }
        return "ENGLISH";
    }

    private String type(int type) {
        switch (type) {
            case CharacterUtil.QUOTE_CHAR:
                return "TYPE_QUOTE";
            case CharacterUtil.COMMA_CHAR:
                return "TYPE_COMMA";
            case CharacterUtil.CHAR_PUNCTUATION:
                return Constant.FNDType.SYMBOL;
            default:
                return "TYPE_ERROR";
        }
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
        int end;
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

    /**
     * 合并负号:6--3 -> (6) (-) (-3)
     *
     * @param tokens 分词列表
     * @return 合并后的分词列表
     */
    private static List<FocusToken> mergeMinus(List<FocusToken> tokens) {
        List<FocusToken> merge = new ArrayList<>();
        if (tokens.size() < 2) {
            return tokens;
        } else if (tokens.size() == 2) {
            FocusToken first = tokens.get(0);
            FocusToken second = tokens.get(1);
            if (Constant.MINUS.equals(first.getWord()) && Common.isNumber(second.getType())) {
                merge.add(new FocusToken(first.getWord() + second.getWord(), second.getType(), first.getStart(), second.getEnd()));
                return merge;
            }
            return tokens;
        }
        FocusToken first = tokens.remove(0);
        int loop = tokens.size();
        merge.add(first);
        while (loop > 0) {
            loop--;
            FocusToken current = tokens.remove(0);
            if (tokens.isEmpty()) {
                merge.add(current);
                break;
            }
            FocusToken next = tokens.get(0);
            if (Constant.MINUS.equals(current.getWord()) && Common.isNumber(next.getType())) {
                if (first.getWord().length() == 1) {
                    int type = CharacterUtil.identifyCharType(first.getWord().charAt(0));
                    if (CharacterUtil.QUOTE_CHAR == type || CharacterUtil.COMMA_CHAR == type || CharacterUtil.CHAR_PUNCTUATION == type) {
                        merge.add(new FocusToken(current.getWord() + next.getWord(), next.getType(), current.getStart(), next.getEnd()));
                        tokens.remove(0);
                        loop--;
                        first = next;
                        continue;
                    }
                }
            }
            merge.add(current);
            first = current;
        }
        return merge;
    }

    /**
     * 合并部分关键词(中文): 系统中有关键词：的，的平均值
     * 输入“的平” -> 分词结果为：(的：keyword) (平：error)
     * “的平”是“的平均值”一部分，因此合并分词：(的平：error)
     *
     * @param tokens 分词列表
     * @return 合并后的分词列表
     */
    private static List<FocusToken> mergePrefixWord(List<FocusToken> tokens) {
        int size = tokens.size();
        if (size < 2) {
            return tokens;
        }
        if (!"TYPE_ERROR".equals(tokens.get(size - 1).getType())) {
            return tokens;
        }
        FocusToken lastSecond = tokens.get(size - 2);
        FocusToken last = tokens.get(size - 1);
        if (lastSecond.getEnd() != last.getStart()) {
            return tokens;
        }
        List<FocusToken> merge = new ArrayList<>();
        merge.addAll(tokens.subList(0, size - 2));
        String word = lastSecond.getWord() + last.getWord();
        List<String> allWords = Dictionary.allWords();
        boolean mergeWords = false;
        for (String keyword : allWords) {
            if (keyword.startsWith(word)) {
                last.setWord(word);
                last.setStart(lastSecond.getStart());
                merge.add(last);
                mergeWords = true;
                break;
            }
        }
        if (!mergeWords) {
            merge.add(lastSecond);
            merge.add(last);
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
