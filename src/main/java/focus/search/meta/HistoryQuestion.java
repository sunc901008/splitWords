package focus.search.meta;

import focus.search.analyzer.focus.FocusToken;

import java.util.List;
import java.util.UUID;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class HistoryQuestion {
    public String question;
    public String sourceList;
    public String taskId;
    public int score;

    public HistoryQuestion() {
    }

    // tokens 作用: 过滤多余空格，记录标准的question(原因：两个tokens之间多个空格其实是无效的)
    public HistoryQuestion(List<FocusToken> tokens, String sourceList, String taskId) {
        this.question = FocusToken.tokensToString(tokens);
        this.sourceList = sourceList;
        this.taskId = taskId;
        this.score = 0;
    }

    public HistoryQuestion(String question, String sourceList) {
        this.question = question;
        this.sourceList = sourceList;
        this.taskId = UUID.randomUUID().toString();
        this.score = 0;
    }

    /**
     * @param history 上一次出正确结果的搜索
     * @param current 当前的搜索
     * @return 当前搜索和上一次搜索相同，则返回空，否则返回上一次的taskId
     */
    public static String equals(HistoryQuestion history, HistoryQuestion current) {
        return history.question.equals(current.question) ? null : history.taskId;
    }

}
