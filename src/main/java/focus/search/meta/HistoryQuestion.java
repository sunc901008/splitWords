package focus.search.meta;

import focus.search.analyzer.focus.FocusToken;

import java.util.List;
import java.util.Objects;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class HistoryQuestion {
    public String question;
    public List<FocusToken> tokens;
    public String taskId;

    public HistoryQuestion(String question, List<FocusToken> tokens, String taskId) {
        this.question = question;
        this.tokens = tokens;
        this.taskId = taskId;
    }

    /**
     * @param history 上一次出正确结果的搜索
     * @param current 当前的搜索
     * @return 当前搜索和上一次搜索相同，则返回空，否则返回上一次的taskId
     */
    public static String equals(HistoryQuestion history, HistoryQuestion current) {
        if (equals(history.tokens, current.tokens)) {
            if (history.question.length() > current.question.length()) {
                history.question = current.question;
            }
            return null;
        }
        return history.taskId;
    }

    private static boolean equals(List<FocusToken> tokens1, List<FocusToken> tokens2) {
        if (tokens1.size() != tokens2.size()) {
            return false;
        }
        for (int i = 0; i < tokens1.size(); i++) {
            if (!Objects.equals(tokens1.get(i).getWord().toLowerCase(), tokens2.get(i).getWord().toLowerCase())) {
                return false;
            }
        }
        return true;
    }

}
