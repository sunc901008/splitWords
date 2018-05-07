package focus.search.response.api;

import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * creator: sunc
 * date: 2018/5/7
 * description:
 */
public class AnswerCheckResponse {

    public Set<Integer> affectAnswers = new HashSet<>();

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("status", "success");
        json.put("affectAnswers", affectAnswers);
        return json;
    }

}
