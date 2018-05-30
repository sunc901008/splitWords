package focus.search.meta;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class HistoryQuestion {
    public String question;
    public JSONObject instruction;
    public String taskId;

    public HistoryQuestion(String question, JSONObject instruction, String taskId) {
        this.question = question;
        this.instruction = instruction;
        this.taskId = taskId;
    }

    /**
     * @param history 上一次出正确结果的搜索
     * @param current 当前的搜索
     * @return 当前搜索和上一次搜索相同，则返回空，否则返回上一次的taskId
     */
    public static String equals(HistoryQuestion history, HistoryQuestion current) {
        if (history.question.equals(current.question) && history.instruction.equals(current.instruction)) {
            return null;
        }
        return history.taskId;
    }

}
