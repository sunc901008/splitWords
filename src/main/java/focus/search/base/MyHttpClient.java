package focus.search.base;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

/**
 * creator: sunc
 * date: 2018/2/2
 * description:
 */
public class MyHttpClient {
    private static final Logger logger = Logger.getLogger(MyHttpClient.class);

    private static final int timeOut = 10;
    private static final RequestConfig defaultConfig = RequestConfig.custom().setConnectTimeout(timeOut).build();

    private static JSONObject toJson(HttpEntityEnclosingRequestBase requestBase, String uuid) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        requestBase.setConfig(defaultConfig);
        String res = "";
        try {
            CloseableHttpResponse response = httpclient.execute(requestBase);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                res = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
            }
        } catch (IOException e) {
            logger.error(Common.printStacktrace(e));
        }
        String msg = uuid + ":request response:" + res;
        logger.info(Common.cut(msg));
        if (res.isEmpty()) {
            return new JSONObject();
        }
        return JSONObject.parseObject(res);
    }

    public static JSONObject get(String url, String entity, List<Header> headers) {
        return request(url, "get", entity, headers);
    }

    public static JSONObject post(String url, String entity, List<Header> headers) {
        return request(url, "post", entity, headers);
    }

    public static JSONObject delete(String url, String entity, List<Header> headers) {
        return request(url, "delete", entity, headers);
    }

    public static JSONObject put(String url, String entity, List<Header> headers) {
        return request(url, "put", entity, headers);
    }

    private static JSONObject request(String url, String method, String entity, List<Header> headers) {
        String uuid = UUID.randomUUID().toString();
        String msg = uuid + ":request. url:" + url + " method:" + method + " params:" + entity;
        logger.info(msg);
        HttpEntityEnclosingRequestBase request;
        switch (method) {
            case "get":
                request = new MyHttpGet(url);
                break;
            case "post":
                request = new HttpPost(url);
                break;
            case "delete":
                request = new MyHttpDelete(url);
                break;
            case "put":
                request = new HttpPut(url);
                break;
            default:
                return new JSONObject();

        }
        if (entity != null) {
            request.setEntity(new StringEntity(entity, Charset.forName("UTF-8")));
        }
        if (headers != null) {
            for (Header header : headers) {
                request.addHeader(header);
            }
        }
        return toJson(request, uuid);
    }

    private static class MyHttpDelete extends HttpEntityEnclosingRequestBase {
        private static final String METHOD_NAME = "DELETE";

        public String getMethod() {
            return METHOD_NAME;
        }

        MyHttpDelete(final String uri) {
            super();
            setURI(URI.create(uri));
        }

    }

    private static class MyHttpGet extends HttpEntityEnclosingRequestBase {
        private static final String METHOD_NAME = "GET";

        public String getMethod() {
            return METHOD_NAME;
        }

        MyHttpGet(final String uri) {
            super();
            setURI(URI.create(uri));
        }

    }
}
