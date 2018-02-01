package focus.search.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.socket.WebSocketSession;

/**
 * creator: sunc
 * date: 2018/1/31
 * description:
 */
class SearchHandler {

    static void preHandle(WebSocketSession session, JSONObject params) {
        String type = params.getString("type");
        switch (type) {
            case "init":
                init(session, params);
                break;
            case "search":
                search(session, params);
                break;
            case "selectSuggest":
                selectSuggest(session, params);
                break;
            case "disambiguate":
                disambiguate(session, params);
                break;
            case "reDisambiguate":
                reDisambiguate(session, params);
                break;
            case "clearDisambiguate":
                clearDisambiguate(session, params);
                break;
            case "axis":
                axis(session, params);
                break;
            case "test":
                test(session, params);
                break;
            case "formula":
                formula(session, params);
                break;
            case "fnamecheck":
                fnamecheck(session, params);
                break;
            case "lang":
                lang(session, params);
                break;
            case "category":
                category(session, params);
                break;
            case "exportContext":
                exportContext(session, params);
                break;
            case "importContext":
                importContext(session, params);
                break;
            case "putFormula":
                putFormula(session, params);
                break;
            case "modifyFormula":
                modifyFormula(session, params);
                break;
            case "deleteFormula":
                deleteFormula(session, params);
                break;
            case "formulaCase":
                formulaCase(session, params);
                break;
            case "echo":
                echo(session, params);
                break;
            default:
        }
    }

    private static void init(WebSocketSession session, JSONObject params) {

    }

    private static void search(WebSocketSession session, JSONObject params) {

    }

    private static void selectSuggest(WebSocketSession session, JSONObject params) {

    }

    private static void disambiguate(WebSocketSession session, JSONObject params) {

    }

    private static void reDisambiguate(WebSocketSession session, JSONObject params) {

    }

    private static void clearDisambiguate(WebSocketSession session, JSONObject params) {

    }

    private static void axis(WebSocketSession session, JSONObject params) {

    }

    private static void test(WebSocketSession session, JSONObject params) {

    }

    private static void formula(WebSocketSession session, JSONObject params) {

    }

    private static void fnamecheck(WebSocketSession session, JSONObject params) {

    }

    private static void lang(WebSocketSession session, JSONObject params) {

    }

    private static void category(WebSocketSession session, JSONObject params) {

    }

    private static void exportContext(WebSocketSession session, JSONObject params) {

    }

    private static void importContext(WebSocketSession session, JSONObject params) {

    }

    private static void putFormula(WebSocketSession session, JSONObject params) {

    }

    private static void modifyFormula(WebSocketSession session, JSONObject params) {

    }

    private static void deleteFormula(WebSocketSession session, JSONObject params) {

    }

    private static void formulaCase(WebSocketSession session, JSONObject params) {

    }

    private static void echo(WebSocketSession session, JSONObject params) {

    }

}
