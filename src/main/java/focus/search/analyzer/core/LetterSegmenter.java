package focus.search.analyzer.core;

import java.util.Arrays;
import java.util.List;

/**
 * 英文字符及阿拉伯数字子分词器
 */
class LetterSegmenter implements ISegmenter {

    // 子分词器标签
    private static final String SEGMENTER_NAME = "LETTER_SEGMENTER";

    // 常用连接符
    public static final List<Character> CONNECT_SYMBOL = Arrays.asList('_', '@');

    /*
     * 词元的开始位置， 同时作为子分词器状态标识 当start > -1 时，标识当前的分词器正在处理字符
     */
    private int start;
    /*
     * 记录词元结束位置 end记录的是在词元中最后一个出现的Letter但非Sign_Connector的字符的位置
     */
    private int end;

    /*
     * 字母起始位置
     */
    private int englishStart;

    /*
     * 字母结束位置
     */
    private int englishEnd;

    LetterSegmenter() {
        this.start = -1;
        this.end = -1;
        this.englishStart = -1;
        this.englishEnd = -1;
    }

    public void analyze(AnalyzeContext context) {
        // 处理英文字母
        boolean bufferLockFlag = this.processEnglishLetter(context);
        // 处理混合字母(这个要放最后处理，可以通过QuickSortSet排除重复)
        bufferLockFlag = bufferLockFlag || this.processMixLetter(context);

        // 判断是否锁定缓冲区
        if (bufferLockFlag) {
            context.lockBuffer(SEGMENTER_NAME);
        } else {
            // 对缓冲区解锁
            context.unlockBuffer(SEGMENTER_NAME);
        }
    }

    public void reset() {
        this.start = -1;
        this.end = -1;
        this.englishStart = -1;
        this.englishEnd = -1;
    }

    /**
     * 处理数字字母混合输出
     */
    private boolean processMixLetter(AnalyzeContext context) {

        if (this.start == -1) {// 当前的分词器尚未开始处理字符
            if (CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()
                    || CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()) {
                // 记录起始指针的位置,标明分词器进入处理状态
                this.start = context.getCursor();
                this.end = start;
            }
        } else {// 当前的分词器正在处理字符
            if (CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()
                    || CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()
                    || CharacterUtil.CHAR_USELESS == context.getCurrentCharType()) {
                // 记录下可能的结束位置
                this.end = context.getCursor();

            } else {
                // 遇到非Letter字符，输出词元
                if (isMixLetter(String.valueOf(context.getSegmentBuff(), this.start, this.end - this.start + 1))) {
                    Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.start, this.end - this.start + 1, Lexeme.TYPE_LETTER);
                    context.addLexeme(newLexeme);
                }
                this.start = -1;
                this.end = -1;
            }
        }

        // 判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            if (this.start != -1 && this.end != -1) {
                // 缓冲以读完，输出词元
                if (isMixLetter(String.valueOf(context.getSegmentBuff(), this.start, this.end - this.start + 1))) {
                    Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.start, this.end - this.start + 1, Lexeme.TYPE_LETTER);
                    context.addLexeme(newLexeme);
                }
                this.start = -1;
                this.end = -1;
            }
        }

        // 判断是否锁定缓冲区
        return !(this.start == -1 && this.end == -1);
    }

    private boolean isMixLetter(String text) {
        boolean letter = false;
        boolean unLetter = false;
        for (char input : text.toCharArray()) {
            if (input == ' ') {
                return letter && unLetter;
            }
            // 含有非英文字符
            if ((input >= 'a' && input <= 'z') || (input >= 'A' && input <= 'Z') || LetterSegmenter.CONNECT_SYMBOL.contains(input)) {
                letter = true;
            } else {
                unLetter = true;
            }
            if (letter && unLetter)
                return true;
        }
        return false;
    }

    /**
     * 处理纯英文字母输出
     */
    private boolean processEnglishLetter(AnalyzeContext context) {
        if (this.englishStart == -1) {// 当前的分词器尚未开始处理英文字符
            if (CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()) {
                // 记录起始指针的位置,标明分词器进入处理状态
                this.englishStart = context.getCursor();
                this.englishEnd = this.englishStart;
            }
        } else {// 当前的分词器正在处理英文字符
            if (CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()) {
                // 记录当前指针位置为结束位置
                this.englishEnd = context.getCursor();
            } else {
                // 遇到非English字符,输出词元
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.englishStart, this.englishEnd
                        - this.englishStart + 1, Lexeme.TYPE_ENGLISH);
                context.addLexeme(newLexeme);
                this.englishStart = -1;
                this.englishEnd = -1;
            }
        }

        // 判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            if (this.englishStart != -1 && this.englishEnd != -1) {
                // 缓冲以读完，输出词元
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.englishStart, this.englishEnd
                        - this.englishStart + 1, Lexeme.TYPE_ENGLISH);
                context.addLexeme(newLexeme);
                this.englishStart = -1;
                this.englishEnd = -1;
            }
        }

        // 判断是否锁定缓冲区
        return !(this.englishStart == -1 && this.englishEnd == -1);
    }

}
