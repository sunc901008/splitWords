package focus.search.analyzer.core;

import focus.search.analyzer.dic.Hit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class CN_QuantifierSegmenter implements ISegmenter {

    // 子分词器标签
    private static final String SEGMENTER_NAME = "QUAN_SEGMENTER";

    private static Set<Character> ChnNumberChars = new HashSet<>();

    static {
        String chn_Num = "一二两三四五六七八九十零壹贰叁肆伍陆柒捌玖拾百千万亿拾佰仟萬億兆卅廿";
        char[] ca = chn_Num.toCharArray();
        for (char nChar : ca) {
            ChnNumberChars.add(nChar);
        }
    }

    /*
     * 词元的开始位置， 同时作为子分词器状态标识 当start > -1 时，标识当前的分词器正在处理字符
     */
    private int nStart;
    /*
     * 记录词元结束位置 end记录的是在词元中最后一个出现的合理的数词结束
     */
    private int nEnd;

    // 待处理的量词hit队列
    private List<Hit> countHits;

    CN_QuantifierSegmenter() {
        nStart = -1;
        nEnd = -1;
        this.countHits = new LinkedList<>();
    }

    /**
     * 分词
     */
    public void analyze(AnalyzeContext context) {
        // 处理中文数词
        this.processCNumber(context);

        // 判断是否锁定缓冲区
        if (this.nStart == -1 && this.nEnd == -1 && countHits.isEmpty()) {
            // 对缓冲区解锁
            context.unlockBuffer(SEGMENTER_NAME);
        } else {
            context.lockBuffer(SEGMENTER_NAME);
        }
    }

    /**
     * 重置子分词器状态
     */
    public void reset() {
        nStart = -1;
        nEnd = -1;
        countHits.clear();
    }

    /**
     * 处理数词
     */
    private void processCNumber(AnalyzeContext context) {
        if (nStart == -1 && nEnd == -1) {// 初始状态
            if (CharacterUtil.CHAR_CHINESE == context.getCurrentCharType()
                    && ChnNumberChars.contains(context.getCurrentChar())) {
                // 记录数词的起始、结束位置
                nStart = context.getCursor();
                nEnd = context.getCursor();
            }
        } else {// 正在处理状态
            if (CharacterUtil.CHAR_CHINESE == context.getCurrentCharType()
                    && ChnNumberChars.contains(context.getCurrentChar())) {
                // 记录数词的结束位置
                nEnd = context.getCursor();
            } else {
                // 输出数词
                this.outputNumLexeme(context);
                // 重置头尾指针
                nStart = -1;
                nEnd = -1;
            }
        }

        // 缓冲区已经用完，还有尚未输出的数词
        if (context.isBufferConsumed()) {
            if (nStart != -1 && nEnd != -1) {
                // 输出数词
                outputNumLexeme(context);
                // 重置头尾指针
                nStart = -1;
                nEnd = -1;
            }
        }

    }

    /**
     * 添加数词词元到结果集
     */
    private void outputNumLexeme(AnalyzeContext context) {
        if (nStart > -1 && nEnd > -1) {
            // 输出数词
            Lexeme newLexeme = new Lexeme(context.getBufferOffset(), nStart, nEnd - nStart + 1, Lexeme.TYPE_CNUM);
            context.addLexeme(newLexeme);

        }
    }

}
