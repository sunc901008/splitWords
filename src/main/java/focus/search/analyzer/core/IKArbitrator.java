package focus.search.analyzer.core;

import focus.search.base.Constant;
import focus.search.controller.common.Base;
import focus.search.meta.AmbiguitiesRecord;
import focus.search.response.exception.AmbiguitiesException;

import java.util.*;

class IKArbitrator {

    IKArbitrator() {
    }

    /**
     * 分词歧义处理
     */
    void process(AnalyzeContext context) throws AmbiguitiesException {
        QuickSortSet orgLexemes = context.getOrgLexemes();
        Lexeme orgLexeme = orgLexemes.pollFirst();

        LexemePath crossPath = new LexemePath();
        while (orgLexeme != null) {
            if (!crossPath.addCrossLexeme(orgLexeme)) {
                // 找到与crossPath不相交的下一个crossPath
                if (crossPath.size() == 1) {
                    // crossPath没有歧义 或者 不做歧义处理
                    // 直接输出当前crossPath
                    context.addLexemePath(crossPath);
                } else {
                    // 对当前的crossPath进行歧义处理
                    QuickSortSet.Cell headCell = crossPath.getHead();
                    LexemePath judgeResult = this.judge(headCell, crossPath, context);
                    // 输出歧义处理结果judgeResult
                    context.addLexemePath(judgeResult);
                }

                // 把orgLexeme加入新的crossPath中
                crossPath = new LexemePath();
                crossPath.addCrossLexeme(orgLexeme);
            }
            orgLexeme = orgLexemes.pollFirst();
        }

        // 处理最后的path
        if (crossPath.size() == 1) {
            // crossPath没有歧义 或者 不做歧义处理
            // 直接输出当前crossPath
            context.addLexemePath(crossPath);
        } else {
            // 对当前的crossPath进行歧义处理
            QuickSortSet.Cell headCell = crossPath.getHead();
            LexemePath judgeResult = this.judge(headCell, crossPath, context);
            // 输出歧义处理结果judgeResult
            context.addLexemePath(judgeResult);
        }
    }

    /**
     * 歧义识别
     *
     * @param lexemeCell 歧义路径链表头
     * @param crossPath  歧义路径文本
     * @return
     */
    private LexemePath judge(QuickSortSet.Cell lexemeCell, LexemePath crossPath, AnalyzeContext context) throws AmbiguitiesException {
        // 候选路径集合
        TreeSet<LexemePath> pathOptions = new TreeSet<>();
        // 候选结果路径
        LexemePath option = new LexemePath();

        // 对crossPath进行一次遍历,同时返回本次遍历中有冲突的Lexeme栈
        Stack<QuickSortSet.Cell> lexemeStack = this.forwardPath(lexemeCell, option);

        // 当前词元链并非最理想的，加入候选路径集合
        pathOptions.add(option.copy());

        // 存在歧义词，处理
        QuickSortSet.Cell c;
        while (!lexemeStack.isEmpty()) {
            c = lexemeStack.pop();
            // 回滚词元链
            this.backPath(c.getLexeme(), option);
            // 从歧义词位置开始，递归，生成可选方案
            this.forwardPath(c, option);
            pathOptions.add(option.copy());
        }
        int begin = crossPath.getPathBegin();
        int end = crossPath.getPathEnd();

        // 返回集合中的最优方案
        // 有多种合法的分词时，提示歧义
//        return pathOptions.first();
        return checkAmbiguity(pathOptions, begin, end, context);
    }

    private LexemePath checkAmbiguity(TreeSet<LexemePath> pathOptions, int begin, int end, AnalyzeContext context) throws AmbiguitiesException {
        Queue<LexemePath> queue = new LinkedList<>();
        while (!pathOptions.isEmpty()) {
            LexemePath lexemePath = pathOptions.pollFirst();
            if (lexemePath.getPathBegin() == begin && lexemePath.getPathEnd() == end) {
                int loop = lexemePath.size();
                List<Lexeme> list = new ArrayList<>();
                while (loop > 0) {
                    loop--;
                    Lexeme lexeme = lexemePath.pollFirst();
                    list.add(lexeme);
                    lexemePath.addLexeme(lexeme);
                }
                boolean match = true;
                int start = begin;
                for (Lexeme lexeme : list) {
                    if (lexeme.getBegin() == start) {
                        start = lexeme.getBegin() + lexeme.getLength();
                    } else {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    queue.add(lexemePath);
                }
            }
        }
        //为英文数字混合，按照最长匹配，返回第一个
        Lexeme first = queue.peek().peekFirst();
        if (first != null && Objects.equals(Lexeme.TYPE_LETTER, first.getLexemeType())) {
            return queue.poll();
        }
        if (!checkAmbiguity(queue, context)) {// 有歧义
            String realValue = String.valueOf(context.getSegmentBuff(), begin, end - begin);
            List<AmbiguitiesRecord> ars = new ArrayList<>();
            while (!queue.isEmpty()) {
                AmbiguitiesRecord ar = new AmbiguitiesRecord(Constant.AmbiguityType.CHINESE, realValue);
                LexemePath tmp = queue.poll();
                StringBuilder possibleValue = new StringBuilder();
                while (tmp.size() > 0) {
                    Lexeme lexeme = tmp.pollFirst();
                    possibleValue.append(String.valueOf(context.getSegmentBuff(), lexeme.getBegin(), lexeme.getLength())).append(" ");
                }
                ar.possibleValue = possibleValue.toString().trim();
                ars.add(ar);
            }
            throw new AmbiguitiesException(ars, begin, end);
        }
        return queue.poll();
    }

    /**
     * 都为关键词的话，按照最长匹配，返回第一个
     *
     * @param queue LexemePath 队列
     * @return 分词是否都为关键词
     */
    private boolean checkAmbiguity(Queue<LexemePath> queue, AnalyzeContext context) {
        if (queue.size() > 1) {
            Queue<LexemePath> tmp = new LinkedList<>(queue);
            while (!tmp.isEmpty()) {
                LexemePath lexemePath = tmp.poll();
                if (lexemePath.size() > 0) {
                    Lexeme lexeme = lexemePath.peekFirst();
                    String str = String.valueOf(context.getSegmentBuff(), lexeme.getBegin(), lexeme.getLength());
                    if (!Base.chineseParser.isKeyword(str)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 向前遍历，添加词元，构造一个无歧义词元组合
     */
    private Stack<QuickSortSet.Cell> forwardPath(QuickSortSet.Cell lexemeCell, LexemePath option) {
        // 发生冲突的Lexeme栈
        Stack<QuickSortSet.Cell> conflictStack = new Stack<>();
        QuickSortSet.Cell c = lexemeCell;
        // 迭代遍历Lexeme链表
        while (c != null && c.getLexeme() != null) {
            if (!option.addNotCrossLexeme(c.getLexeme())) {
                // 词元交叉，添加失败则加入lexemeStack栈
                conflictStack.push(c);
            }
            c = c.getNext();
        }
        return conflictStack;
    }

    /**
     * 回滚词元链，直到它能够接受指定的词元
     *
     * @param l
     */
    private void backPath(Lexeme l, LexemePath option) {
        while (option.checkCross(l)) {
            option.removeTail();
        }

    }

}
