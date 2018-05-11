package focus.search.base;

import com.alibaba.fastjson.JSONObject;
import focus.search.response.exception.FocusHttpException;
import org.apache.http.Header;
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

    private static JSONObject get(String url, String entity, List<Header> headers) throws FocusHttpException {
        JSONObject res = MyHttpClient.get(url, entity, headers);
        if (res.isEmpty()) {
            throw new FocusHttpException(url, entity, headers);
        }
        return res;
    }

    private static JSONObject post(String url, String entity, List<Header> headers) throws FocusHttpException {
        JSONObject res = MyHttpClient.post(url, entity, headers);
        if (res.isEmpty()) {
            throw new FocusHttpException(url, entity, headers);
        }
        return res;
    }

    private static void delete(String url, String entity, List<Header> headers) throws FocusHttpException {
        JSONObject res = MyHttpClient.delete(url, entity, headers);
        if (res.isEmpty()) {
            throw new FocusHttpException(url, entity, headers);
        }
    }

    public static class Bi {
        private static String baseUrl = String.format("http://%s:%d%s/", Constant.biHost, Constant.biPort, Constant.biBaseUrl);
        private static final String CHECK_QUERY = "checkQuery";
        private static final String QUERY = "query";

        public static JSONObject checkQuery(String params) throws FocusHttpException {
            return post(baseUrl + CHECK_QUERY, params, Collections.singletonList(baseHeader));
        }

        public static JSONObject query(String params) throws FocusHttpException {
            return post(baseUrl + QUERY, params, Collections.singletonList(baseHeader));
        }

        public static void abortQuery(String params) throws FocusHttpException {
            delete(baseUrl + QUERY, params, Collections.singletonList(baseHeader));
        }
    }

    public static class WebServer {

        private static String baseUrl = String.format("http://%s:%d%s/", Constant.webServerHost, Constant.webServerPort, Constant.webServerBaseUrl);
        private static final String GET_SOURCE = "getSource";

        public static JSONObject getSource(String sourceToken) throws FocusHttpException {
            BasicHeader header = new BasicHeader("sourceToken", sourceToken);
            return get(baseUrl + GET_SOURCE, null, Arrays.asList(header, baseHeader));
        }

    }

    public static class Uc {

        private static String baseUrl = String.format("http://%s:%d%s/", Constant.ucHost, Constant.ucPort, Constant.ucBaseUrl);
        private static final String USERINFO = "user/userinfo";
        private static final String STATUS = "status";

        public static boolean isLogin(String accessToken) throws FocusHttpException {
            if (Common.isEmpty(accessToken))
                return false;
            JSONObject res = get(baseUrl + STATUS, null, Arrays.asList(new BasicHeader("Access-Token", accessToken), baseHeader));
            return res.getString("status").equals("success");
        }

        public static JSONObject getUserInfo(String accessToken) throws FocusHttpException {
            String url = baseUrl + USERINFO;
            List<Header> headers = Arrays.asList(new BasicHeader("Access-Token", accessToken), baseHeader);
            JSONObject response = get(url, null, headers);
            if (response.getString("status").equals("success")) {
                return response.getJSONObject("user");
            } else {
                throw new FocusHttpException(url, null, headers);
            }
        }

    }

}
