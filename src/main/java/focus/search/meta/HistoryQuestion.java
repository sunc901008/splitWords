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

    public static String equals(HistoryQuestion history, HistoryQuestion current) {
        if (history.question.equals(current.question) && history.instruction.equals(current.instruction)) {
            return history.taskId;
        }
        return null;
    }

}
