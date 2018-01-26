package focus.search.analyzer.core;

import focus.search.analyzer.dic.Hit;
import focus.search.analyzer.focus.FocusSuggestions;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

/**
 * 分词器上下文状态
 */
class AnalyzeContext {

    // 默认缓冲区大小
    private static final int BUFF_SIZE = 4096;
    // 缓冲区耗尽的临界值
    private static final int BUFF_EXHAUST_CRITICAL = 100;

    // 字符窜读取缓冲
    private char[] segmentBuff;
    // 字符类型数组
    private int[] charTypes;

    // 记录Reader内已分析的字串总长度
    // 在分多段分析词元时，该变量累计当前的segmentBuff相对于reader起始位置的位移
    private int buffOffset;
    // 当前缓冲区位置指针
    private int cursor;
    // 最近一次读入的,可处理的字串长度
    private int available;

    // 子分词器锁
    // 该集合非空，说明有子分词器在占用segmentBuff
    private Set<String> buffLocker;

    // 原始分词结果集合，未经歧义处理
    private QuickSortSet orgLexemes;
    // LexemePath位置索引表
    private Map<Integer, LexemePath> pathMap;
    // 最终分词结果集
    private LinkedList<Lexeme> results;

    private FocusSegmenter focusSegmenter = new FocusSegmenter();

    public AnalyzeContext() {
        this.segmentBuff = new char[BUFF_SIZE];
        this.charTypes = new int[BUFF_SIZE];
        this.buffLocker = new HashSet<>();
        this.orgLexemes = new QuickSortSet();
        this.pathMap = new HashMap<>();
        this.results = new LinkedList<>();
    }

    int getCursor() {
        return this.cursor;
    }

    char[] getSegmentBuff() {
        return this.segmentBuff;
    }

    char getCurrentChar() {
        return this.segmentBuff[this.cursor];
    }

    int getCurrentCharType() {
        return this.charTypes[this.cursor];
    }

    int getBufferOffset() {
        return this.buffOffset;
    }

    /**
     * 根据context的上下文情况，填充segmentBuff
     *
     * @return 返回待分析的（有效的）字串长度
     */
    int fillBuffer(Reader reader) throws IOException {
        int readCount = 0;
        if (this.buffOffset == 0) {
            // 首次读取reader
            readCount = reader.read(segmentBuff);
        } else {
            int offset = this.available - this.cursor;
            if (offset > 0) {
                // 最近一次读取的>最近一次处理的，将未处理的字串拷贝到segmentBuff头部
                System.arraycopy(this.segmentBuff, this.cursor, this.segmentBuff, 0, offset);
                readCount = offset;
            }
            // 继续读取reader ，以onceReadIn - onceAnalyzed为起始位置，继续填充segmentBuff剩余的部分
            readCount += reader.read(this.segmentBuff, offset, BUFF_SIZE - offset);
        }
        // 记录最后一次从Reader中读入的可用字符长度
        this.available = readCount;
        // 重置当前指针
        this.cursor = 0;
        return readCount;
    }

    /**
     * 初始化buff指针，处理第一个字符
     */
    void initCursor() {
        this.cursor = 0;
        this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
        this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
    }

    /**
     * 指针+1
     * 成功返回 true； 指针已经到了buff尾部，不能前进，返回false
     * 并处理当前字符
     */
    boolean moveCursor() {
        if (this.cursor < this.available - 1) {
            this.cursor++;
            this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
            this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置当前segmentBuff为锁定状态
     * 加入占用segmentBuff的子分词器名称，表示占用segmentBuff
     */
    void lockBuffer(String segmenterName) {
        this.buffLocker.add(segmenterName);
    }

    /**
     * 移除指定的子分词器名，释放对segmentBuff的占用
     */
    void unlockBuffer(String segmenterName) {
        this.buffLocker.remove(segmenterName);
    }

    /**
     * 只要buffLocker中存在segmenterName
     * 则buffer被锁定
     *
     * @return boolean 缓冲去是否被锁定
     */
    private boolean isBufferLocked() {
        return this.buffLocker.size() > 0;
    }

    /**
     * 判断当前segmentBuff是否已经用完
     * 当前执针cursor移至segmentBuff末端this.available - 1
     */
    boolean isBufferConsumed() {
        return this.cursor == this.available - 1;
    }

    /**
     * 判断segmentBuff是否需要读取新数据
     * <p>
     * 满足一下条件时，
     * 1.available == BUFF_SIZE 表示buffer满载
     * 2.buffIndex < available - 1 && buffIndex > available - BUFF_EXHAUST_CRITICAL表示当前指针处于临界区内
     * 3.!context.isBufferLocked()表示没有segmenter在占用buffer
     * 要中断当前循环（buffer要进行移位，并再读取数据的操作）
     */
    boolean needRefillBuffer() {
        return this.available == BUFF_SIZE && this.cursor < this.available - 1
                && this.cursor > this.available - BUFF_EXHAUST_CRITICAL && !this.isBufferLocked();
    }

    /**
     * 累计当前的segmentBuff相对于reader起始位置的位移
     */
    void markBufferOffset() {
        this.buffOffset += this.cursor;
    }

    /**
     * 向分词结果集添加词元
     */
    void addLexeme(Lexeme lexeme) {
        this.orgLexemes.addLexeme(lexeme);
    }

    /**
     * 添加分词结果路径
     * 路径起始位置 ---> 路径 映射表
     */
    void addLexemePath(LexemePath path) {
        if (path != null) {
            this.pathMap.put(path.getPathBegin(), path);
        }
    }

    /**
     * 返回原始分词结果
     */
    QuickSortSet getOrgLexemes() {
        return this.orgLexemes;
    }

    /**
     * 推送分词结果到结果集合
     * 1.从buff头部遍历到this.cursor已处理位置
     * 2.将map中存在的分词结果推入results
     * 3.将map中不存在的CJDK字符以单字方式推入results
     */
    void outputToResult() throws IOException {
        int index = 0;
        for (; index <= this.cursor; ) {
            if (CharacterUtil.CHAR_USELESS == this.charTypes[index]) {
                index++;
                continue;
            }

//            AnalyzeContext context = new AnalyzeContext();
            // 从pathMap找出对应index位置的LexemePath
            LexemePath path = this.pathMap.get(index);
            if (path != null) {
                // 输出LexemePath中的lexeme到results集合
                Lexeme l = path.pollFirst();
                while (l != null) {
                    // 将index移至lexeme后
                    index = l.getBegin() + l.getLength();
//                    if (l.getType() != null) {
//                        String text = String.valueOf(this.segmentBuff, l.getBegin(), l.getLength());
//                        context.fillBuffer(new StringReader(text));
//                        context.initCursor();
//                        do {
//                            focusSegmenter.analyze(context);
//                            if (context.needRefillBuffer()) {
//                                break;
//                            }
//                        } while (context.moveCursor());
//                        Lexeme tmp = context.orgLexemes.peekFirst();
//                        context.reset();
//                        focusSegmenter.reset();
//                        for (String ambiguity : tmp.getAmbiguity()) {
//                            l.addAmbiguity(ambiguity);
//                        }
//                    }
                    this.results.add(l);
                    l = path.pollFirst();
                    if (l != null) {
                        // 输出path内部，词元间遗漏的单字
                        for (; index < l.getBegin(); index++) {
                            this.outputSingleCJK(index);
                        }
                    }
                }
            } else {// pathMap中找不到index对应的LexemePath
                // 错误提示
                String text = String.valueOf(this.segmentBuff, index, available - index);
//                context.fillBuffer(new StringReader(text));
//                context.initCursor();
//                boolean suggest = true;
//                do {
//                    focusSegmenter.analyze(context);
//                    if (focusSegmenter.getTmpHits().size() == 0) {
//                        suggest = false;
//                    }
//                    if (context.needRefillBuffer()) {
//                        break;
//                    }
//                } while (context.moveCursor());
//                context.reset();

                Lexeme singleCharLexeme = new Lexeme(this.buffOffset, index, available - index, Lexeme.TYPE_ERROR);

//                if (suggest) {
//                    singleCharLexeme.setLexemeType(Lexeme.TYPE_SUGGEST);
//                    List<String> exist = new ArrayList<>();
//                    for (Hit hit : focusSegmenter.getTmpHits()) {
//                        for (FocusSuggestions fs : hit.getSuggestions()) {
//                            if (exist.contains(fs.getWord() + "_" + fs.getType())) {
//                                continue;
//                            }
//                            exist.add(fs.getWord() + "_" + fs.getType());
//                            fs.setWord(text + fs.getWord());
//                            singleCharLexeme.addSuggestion(fs);
//                        }
//                    }
//                    exist.clear();
//                }
                focusSegmenter.reset();

                this.results.add(singleCharLexeme);
                break;
            }
        }
        // 清空当前的Map
        this.pathMap.clear();
    }

    /**
     * 对CJK字符进行单字输出
     */
    private void outputSingleCJK(int index) {
        if (CharacterUtil.CHAR_CHINESE == this.charTypes[index]) {
            Lexeme singleCharLexeme = new Lexeme(this.buffOffset, index, 1, Lexeme.TYPE_CNCHAR);
            this.results.add(singleCharLexeme);
        } else if (CharacterUtil.CHAR_OTHER_CJK == this.charTypes[index]) {
            Lexeme singleCharLexeme = new Lexeme(this.buffOffset, index, 1, Lexeme.TYPE_OTHER_CJK);
            this.results.add(singleCharLexeme);
        }
    }

    /**
     * 返回lexeme
     * <p>
     * 同时处理合并
     */
    Lexeme getNextLexeme() {
        // 从结果集取出，并移除第一个Lexme
        Lexeme result = this.results.pollFirst();
        if (result != null) {
            result.setLexemeText(String.valueOf(segmentBuff, result.getBegin(), result.getLength()));
        }
        return result;
    }

    /**
     * 重置分词上下文状态
     */
    void reset() {
        this.buffLocker.clear();
        this.orgLexemes = new QuickSortSet();
        this.available = 0;
        this.buffOffset = 0;
        this.charTypes = new int[BUFF_SIZE];
        this.cursor = 0;
        this.results.clear();
        this.segmentBuff = new char[BUFF_SIZE];
        this.pathMap.clear();
    }

}
