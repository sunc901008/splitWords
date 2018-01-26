package focus.search.analyzer.core;

/**
 * 英文字符及阿拉伯数字子分词器
 */
class PunctuationSegmenter implements ISegmenter {

    // 子分词器标签
    private static final String SEGMENTER_NAME = "PUNCTUATION_SEGMENTER";
    // 数字符号
    public static final char Num_Connector = '.';

    /*
     * 符号起始位置
     */
    private int punctuationStart;

    /*
     * 符号结束位置
     */
    private int punctuationEnd;

    /*
     * 阿拉伯数字起始位置
     */
    private int arabicStart;

    /*
     * 阿拉伯数字结束位置
     */
    private int arabicEnd;

    PunctuationSegmenter() {
        this.punctuationStart = -1;
        this.punctuationEnd = -1;
        this.arabicStart = -1;
        this.arabicEnd = -1;
    }

    public void analyze(AnalyzeContext context) {
        // 处理阿拉伯字母
        boolean bufferLockFlag = this.processArabicLetter(context);
        // 处理符号
        bufferLockFlag = this.processPunctuationLetter(context) || bufferLockFlag;

        // 判断是否锁定缓冲区
        if (bufferLockFlag) {
            context.lockBuffer(SEGMENTER_NAME);
        } else {
            // 对缓冲区解锁
            context.unlockBuffer(SEGMENTER_NAME);
        }
    }

    public void reset() {
        this.punctuationStart = -1;
        this.punctuationEnd = -1;
        this.arabicStart = -1;
        this.arabicEnd = -1;
    }

    /**
     * 处理符号输出
     */
    private boolean processPunctuationLetter(AnalyzeContext context) {
        if (this.punctuationStart == -1) {// 当前的分词器尚未开始处理数字字符
            if (CharacterUtil.CHAR_PUNCTUATION == context.getCurrentCharType()) {
                // 记录起始指针的位置,标明分词器进入处理状态
                this.punctuationStart = context.getCursor();
                this.punctuationEnd = this.punctuationStart;
            }
        } else {// 当前的分词器正在处理数字字符
            if (CharacterUtil.CHAR_PUNCTUATION == context.getCurrentCharType()) {
                // 记录当前指针位置为结束位置
                this.punctuationEnd = context.getCursor();
            } else {
                // 遇到非符号字符,输出词元
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.punctuationStart, this.punctuationEnd
                        - this.punctuationStart + 1, Lexeme.TYPE_PUNC);
                context.addLexeme(newLexeme);
                this.punctuationStart = -1;
                this.punctuationEnd = -1;
            }
        }

        // 判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            if (this.punctuationStart != -1 && this.punctuationEnd != -1) {
                // 生成已切分的词元
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.punctuationStart, this.punctuationEnd - this.punctuationStart + 1, Lexeme.TYPE_PUNC);
                context.addLexeme(newLexeme);
                this.punctuationStart = -1;
                this.punctuationEnd = -1;
            }
        }

        // 判断是否锁定缓冲区
        return !(this.punctuationStart == -1 && this.punctuationStart == -1);
    }


    /**
     * 处理阿拉伯数字输出
     */
    private boolean processArabicLetter(AnalyzeContext context) {
        if (this.arabicStart == -1) {// 当前的分词器尚未开始处理数字字符
            if (CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()) {
                // 记录起始指针的位置,标明分词器进入处理状态
                this.arabicStart = context.getCursor();
                this.arabicEnd = this.arabicStart;
            }
        } else {// 当前的分词器正在处理数字字符
            if (CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()) {
                // 记录当前指针位置为结束位置
                this.arabicEnd = context.getCursor();
            } else {
                // 判断是否是数字连接符号
                addLexeme(context);
            }
        }

        // 判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            if (this.arabicStart != -1 && this.arabicEnd != -1) {
                // 生成已切分的词元
                addLexeme(context);
            }
        }

        // 判断是否锁定缓冲区
        return !(this.arabicStart == -1 && this.arabicEnd == -1);
    }

    private void addLexeme(AnalyzeContext context) {
        Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.arabicStart, this.arabicEnd - this.arabicStart + 1, Lexeme.TYPE_ARABIC);

        newLexeme.setType(getType(String.valueOf(context.getSegmentBuff(), newLexeme.getBegin(), newLexeme.getLength())));

        context.addLexeme(newLexeme);
        this.arabicStart = -1;
        this.arabicEnd = -1;
    }

    private String getType(String text) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return "number";
        }
        return "integer";
    }

}
