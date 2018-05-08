package focus.search.base;

import com.alibaba.fastjson.JSONObject;
import focus.search.response.exception.MyHttpException;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.message.BasicHeader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/2
 * description:
 */
public class Clients {

    private static final BasicHeader baseHeader = new BasicHeader("Content-Type", "application/json");

    private static JSONObject get(String url, String entity, List<Header> headers) throws MyHttpException {
        JSONObject res = MyHttpClient.get(url, entity, headers);
        if (res.isEmpty()) {
            throw new MyHttpException();
        }
        return res;
    }

    private static JSONObject post(String url, String entity, List<Header> headers) throws MyHttpException {
        JSONObject res = MyHttpClient.post(url, entity, headers);
        if (res.isEmpty()) {
            throw new MyHttpException();
        }
        return res;
    }

    private static void delete(String url, String entity, List<Header> headers) throws MyHttpException {
        JSONObject res = MyHttpClient.delete(url, entity, headers);
        if (res.isEmpty()) {
            throw new MyHttpException();
        }
    }

    public static class WebServer {

        private static String baseUrl = String.format("http://%s:%d%s/", Constant.webServerHost, Constant.webServerPort, Constant.webServerBaseUrl);
        private static final String GET_SOURCE = "getSource";
        private static final String QUERY_URL = "query";

        public static JSONObject getSource(String sourceToken) throws MyHttpException {
            BasicHeader header = new BasicHeader("sourceToken", sourceToken);
            return get(baseUrl + GET_SOURCE, null, Arrays.asList(header, baseHeader));
        }

        public static JSONObject query(String params) throws MyHttpException {
            return post(baseUrl + QUERY_URL, params, Collections.singletonList(baseHeader));
        }

        public static void abortQuery(String params) throws MyHttpException {
            delete(baseUrl + QUERY_URL, params, Collections.singletonList(baseHeader));
        }

    }

    public static class Uc {

        private static String baseUrl = String.format("http://%s:%d%s/", Constant.ucHost, Constant.ucPort, Constant.ucBaseUrl);
        private static final String GET_SOURCE = "getSource";

        public static JSONObject getSource(String sourceToken) throws MyHttpException {
            BasicHeader header = new BasicHeader("sourceToken", sourceToken);
            return get(baseUrl + GET_SOURCE, null, Arrays.asList(header, baseHeader));
        }
    }

}
