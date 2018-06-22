package focus.search.response.exception;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class FocusHttpException extends Exception {
    private String url;
    private String params;
    private List<Header> headers = new ArrayList<>();

    public FocusHttpException(String url, String params, List<Header> headers) {
        super("Http connect exception!");
        this.url = url;
        this.params = params;
        this.headers = headers;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("url", url);
        json.put("lastParams", params);
        JSONArray jsonArray = new JSONArray();
        for (Header header : headers) {
            JSONObject h = new JSONObject();
            h.put(header.getName(), header.getValue());
            jsonArray.add(h);
        }
        json.put("headers", jsonArray);
        return json;
    }
}
