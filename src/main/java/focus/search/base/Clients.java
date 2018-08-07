package focus.search.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.meta.Column;
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

        public static JSONObject checkQuery(String params) throws FocusHttpException {
            return post(baseUrl + CHECK_QUERY, params, Collections.singletonList(baseHeader));
        }

    }

    public static class WebServer {

        private static String baseUrl = String.format("http://%s:%d%s/", Constant.webServerHost, Constant.webServerPort, Constant.webServerBaseUrl);
        private static final String GET_SOURCE = "getSource";
        private static final String QUERY = "query";

        public static JSONObject getSource(String sourceToken, String accessToken) throws FocusHttpException {
            BasicHeader header1 = new BasicHeader("sourceToken", sourceToken);
            BasicHeader header2 = new BasicHeader("Access-Token", accessToken);
            return get(baseUrl + GET_SOURCE, null, Arrays.asList(header1, header2, baseHeader));
        }

        public static JSONObject query(String params, String accessToken) throws FocusHttpException {
            BasicHeader header = new BasicHeader("Access-Token", accessToken);
            return post(baseUrl + QUERY, params, Arrays.asList(header, baseHeader));
        }

        public static void abortQuery(String params, String accessToken) throws FocusHttpException {
            BasicHeader header = new BasicHeader("Access-Token", accessToken);
            delete(baseUrl + QUERY, params, Arrays.asList(header, baseHeader));
        }

    }

    public static class Uc {

        private static String baseUrl = String.format("http://%s:%d%s/", Constant.ucHost, Constant.ucPort, Constant.ucBaseUrl);
        private static final String STATUS = baseUrl + "status";

        public static boolean isLogin(String accessToken) throws FocusHttpException {
            if (Common.isEmpty(accessToken))
                return false;
            JSONObject res = post(STATUS, null, Arrays.asList(new BasicHeader("Access-Token", accessToken), baseHeader));
            return res.getBoolean("success");
        }

        public static JSONObject getUserInfo(String accessToken) throws FocusHttpException {
            List<Header> headers = Arrays.asList(new BasicHeader("Access-Token", accessToken), baseHeader);
            JSONObject res = post(STATUS, null, headers);
            if (res.getBoolean("success")) {
                return res.getJSONObject("user");
            } else {
                throw new FocusHttpException(STATUS, null, headers);
            }
        }

    }

    public static class Index {
        private static JSONObject params = new JSONObject();

        static {
            params.put("type", "columnValue");
            params.put("partialToken", "");
            params.put("size", 5);
            params.put("ignoreCase", true);
            params.put("datas", null);
        }

        private static JSONObject tokensParam(Column column, String word, int count) {
            JSONObject source = new JSONObject();
            JSONArray sources = new JSONArray();

            JSONObject sourceInfo = new JSONObject();
            sourceInfo.put("source", column.getTbPhysicalName());
            sourceInfo.put("database", column.getDbName());

            JSONArray columns = new JSONArray();
            columns.add(column.getColumnPhysicalName());
            sourceInfo.put("columns", columns);

            sources.add(sourceInfo);

            source.put("source", sources);
            JSONObject param = JSON.parseObject(params.toJSONString());
            param.put("partialToken", word);
            if (count > 0)
                param.put("size", count);
            param.put("datas", source);
            return param;
        }

        private static String baseUrl = String.format("http://%s:%d%s/", Constant.indexHost, Constant.indexPort, Constant.indexBaseUrl);
        private static final String TOKENS = "tokens";

        public static JSONObject tokens(Column column, String word) throws FocusHttpException {
            return tokens(column, word, 5);
        }

        public static JSONObject tokens(Column column, String word, int count) throws FocusHttpException {
            JSONObject result = new JSONObject();
            JSONArray tokens = new JSONArray();
            JSONObject token = new JSONObject();
            token.put("content", word);
            tokens.add(token);
            result.put("tokens", tokens);
            if (Constant.passIndex) {
                return result;
            }
            return post(baseUrl + TOKENS, tokensParam(column, word, count).toJSONString(), Collections.singletonList(baseHeader));
        }

    }

}
