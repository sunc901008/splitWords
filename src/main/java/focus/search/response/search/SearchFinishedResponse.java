package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

import java.util.Calendar;

/**
 * creator: sunc
 * date: 2018/3/22
 * description:
 */
public class SearchFinishedResponse {

    public static String response(String question, long cost) {
        long now = Calendar.getInstance().getTimeInMillis();
        JSONObject json = new JSONObject();
        json.put("type", "state");
        json.put("question", question);
        json.put("cost", now - cost);
        json.put("datas", "searchFinished");
        return json.toJSONString();
    }

}
