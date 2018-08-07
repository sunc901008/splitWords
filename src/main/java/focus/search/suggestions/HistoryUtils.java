package focus.search.suggestions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Clients;
import focus.search.entity.History;
import focus.search.meta.HistoryQuestion;
import focus.search.response.exception.FocusHttpException;
import focus.search.service.HistoryServiceImpl;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/4
 * description: history utils 处理历史问题的总类
 */
public class HistoryUtils {
    private static final Logger logger = Logger.getLogger(HistoryUtils.class);

    private HistoryServiceImpl historyService;

    private static HistoryUtils hu;

    // 手动注入 focus.search.service.HistoryServiceImpl
    public void setHistoryService(HistoryServiceImpl historyService) {
        this.historyService = historyService;
    }

    public void init() {
        hu = this;
        hu.historyService = this.historyService;
    }

    /**
     * @param user websocket用户信息
     * @return 当前环境信息
     */
    private static JSONObject log(JSONObject user) {
        JSONObject environment = new JSONObject();
        environment.put("userId", user.getInteger("id"));
        environment.put("language", user.getString("language"));
        environment.put("sourceList", user.getString("sourceList"));
        JSONArray historyQuestions = user.getJSONArray("historyQuestions");
        if (historyQuestions != null) {
            environment.put("historyQuestions", historyQuestions);
        }
        return environment;
    }

    /**
     * 初始化历史问题的记录 (从数据库中取出历史问题)
     * <p>
     * 初始化，按照评分大小从前往后排序，记录到当前连接的user信息里
     * 使用过程中，按照最近使用更新顺序，同时更新分数
     *
     * @param user websocket用户信息
     * @return 历史问题列表
     */
    public static JSONArray initHistory(JSONObject user) {
        logger.info("init history. current environment:" + log(user));
        JSONArray historyQuestions = new JSONArray();
        int userId = user.getInteger("id");
        String language = user.getString("language");
        JSONArray sourceList = JSONArray.parseArray(user.getString("sourceList"));
        List<History> histories = hu.historyService.selectByUserId(userId, language);
        for (History history : histories) {
            JSONArray tmp = JSONArray.parseArray(history.sourceList);
            if (sourceList.containsAll(tmp)) {
                historyQuestions.add(new HistoryQuestion(history.question, history.sourceList));
            }
        }
        return historyQuestions;
    }

    /**
     * 记录到历史记录中,并且放弃上一次还未执行完的搜索
     *
     * @param current question, tokens, taskId
     * @param user    websocket 用户信息
     */
    public static void addQuestion(HistoryQuestion current, JSONObject user) throws FocusHttpException {
        logger.info("add history:" + JSONObject.toJSONString(current));
        JSONArray questions = user.getJSONArray("historyQuestions");
        logger.info("current historyQuestions:" + questions.toJSONString());
        if (questions.size() > 0) {
            HistoryQuestion last = (HistoryQuestion) questions.get(0);
            String taskId = HistoryQuestion.equals(last, current);
            if (taskId != null) {
                addQuestion(current, questions);
                Clients.WebServer.abortQuery(taskId, user.getString("accessToken"));
            } else {
                last.score = last.score + 1;
            }
        } else {
            addQuestion(current, questions);
        }
        logger.info("after adding historyQuestions:" + questions.toJSONString());
        user.put("historyQuestions", questions);
    }

    private static void addQuestion(HistoryQuestion current, JSONArray questions) throws FocusHttpException {
        for (Object object : questions) {
            HistoryQuestion hq = (HistoryQuestion) object;
            if (HistoryQuestion.equals(hq, current) == null) {
                questions.remove(object);
                hq.score = hq.score + 1;
                questions.add(0, hq);
                return;
            }
        }
        current.score = 1;
        questions.add(0, current);
    }

    /**
     * 持久化历史问题的记录 (历史问题写入数据库中)
     * <p>
     * 查找，存在则更新，不存在，则新增
     *
     * @param user websocket用户信息
     */
    public static void persistentHistory(JSONObject user) {
        JSONArray historyQuestions = user.getJSONArray("historyQuestions");
        int userId = user.getInteger("id");
        String language = user.getString("language");
        if (historyQuestions.size() > 0) {
            logger.info("persistent history. current environment:" + log(user));
            for (Object object : historyQuestions) {
                HistoryQuestion hq = (HistoryQuestion) object;
                History history = hu.historyService.exist(userId, language, hq.question);
                if (history == null) {
                    history = new History();
                    history.userId = userId;
                    history.score = hq.score;
                    history.language = language;
                    history.question = hq.question;
                    history.sourceList = hq.sourceList;
                    hu.historyService.insert(history);
                } else {
                    history.score = history.score + hq.score;
                    hu.historyService.updateService(history);
                }
            }
            // 持久化完成之后，清空(防止重复记录)
            user.put("historyQuestions", new JSONArray());
        }
    }

}
