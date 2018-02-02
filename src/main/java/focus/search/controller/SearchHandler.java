package focus.search.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Clients;
import focus.search.base.Constant;
import focus.search.base.LoggerHandler;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.Response;
import focus.search.response.search.InitResponse;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/31
 * description:
 */
class SearchHandler {

    static void preHandle(WebSocketSession session, JSONObject params) throws IOException {
        LoggerHandler.info("params: " + params);
        String type = params.getString("type");
        JSONObject datas = params.getJSONObject("datas");
        switch (type) {
            case "init":
                init(session, datas);
                break;
            case "search":
                search(session, datas);
                break;
            case "selectSuggest":
                selectSuggest(session, datas);
                break;
            case "disambiguate":
                disambiguate(session, datas);
                break;
            case "reDisambiguate":
                reDisambiguate(session, datas);
                break;
            case "clearDisambiguate":
                clearDisambiguate(session, datas);
                break;
            case "axis":
                axis(session, datas);
                break;
            case "test":
                test(session, datas);
                break;
            case "formula":
                formula(session, datas);
                break;
            case "fnamecheck":
                fnamecheck(session, datas);
                break;
            case "lang":
                lang(session, datas);
                break;
            case "category":
                category(session, datas);
                break;
            case "exportContext":
                exportContext(session, datas);
                break;
            case "importContext":
                importContext(session, datas);
                break;
            case "putFormula":
                putFormula(session, datas);
                break;
            case "modifyFormula":
                modifyFormula(session, datas);
                break;
            case "deleteFormula":
                deleteFormula(session, datas);
                break;
            case "formulaCase":
                formulaCase(session, datas);
                break;
            case "echo":
            default:
                echo(session);
        }
    }

    private static void init(WebSocketSession session, JSONObject datas) throws IOException {
        String category = datas.getString("category");
        String context = datas.getString("context");
        String curSearchToken = datas.getString("curSearchToken");
        String lang = datas.getString("lang");
        String sourceToken = datas.getString("sourceToken");

        JSONObject getSource;
        try {
            getSource = Clients.WebServer.getSource(sourceToken);
        } catch (Exception e) {
            // todo exception controller
            return;
        }

        InitResponse init = new InitResponse();
        if (!"success".equals(getSource.getString("status"))) {
            init.setStatus("fail");
            init.setMessage("get sources from webserver fail.");
        } else {
            init.setStatus("success");
            init.setMessage("get sources from webserver success.");
            List<SourceReceived> srs = JSONArray.parseArray(getSource.getJSONArray("sources").toJSONString(), SourceReceived.class);
            for (SourceReceived sr : srs) {
                init.addSource(sr.transfer());
            }
        }

        Response response = new Response();
        response.setType("response");
        response.setCommand("init");
        response.setSourceToken(sourceToken);
        response.setDatas(init.toJson());

        LoggerHandler.info(response.toString(), Constant.PRINT_LOG);

        session.sendMessage(new TextMessage(response.toString()));
    }

    private static void search(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void selectSuggest(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void disambiguate(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void reDisambiguate(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void clearDisambiguate(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void axis(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void test(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void formula(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void fnamecheck(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void lang(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void category(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void exportContext(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void importContext(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void putFormula(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void modifyFormula(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void deleteFormula(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void formulaCase(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void echo(WebSocketSession session) throws IOException {
        JSONObject response = new JSONObject();
        response.put("type", "echo");
        session.sendMessage(new TextMessage(response.toJSONString()));
    }

}
