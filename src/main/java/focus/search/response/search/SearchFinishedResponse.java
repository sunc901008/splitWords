package focus.search.response.search;

import com.alibaba.fastjson.JSONObject;

import java.util.Calendar;

/**
 * creator: sunc
 * date: 2018/3/22
 * description:
 */
public class SearchFinishedResponse {

    public static String response(String question) {
        return response(question, 0);
    }

    public static String response(String question, long cost) {
        JSONObject json = new JSONObject();
        json.put("type", "state");
        json.put("question", question);
        if (cost > 0)
            json.put("cost", Calendar.getInstance().getTimeInMillis() - cost);
        json.put("datas", "searchFinished");
        return json.toJSONString();
    }

}
