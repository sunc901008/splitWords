package focus.search.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.base.LoggerHandler;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.InstructionBuild;
import focus.search.metaReceived.Ambiguities;
import focus.search.metaReceived.RelationReceived;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.Response;
import focus.search.response.search.InitResponse;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        JSONObject user = (JSONObject) session.getAttributes().get("user");
        switch (type) {
            case "init":
                init(session, datas, user);
                break;
            case "search":
                search(session, datas, user);
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
                echo(session, user);
        }
    }

    private static void init(WebSocketSession session, JSONObject datas, JSONObject user) throws IOException {
        String category = datas.getString("category");
        String context = datas.getString("context");
        String curSearchToken = datas.getString("curSearchToken");
        String language = datas.getString("lang");
        String sourceToken = datas.getString("sourceToken");

        user.put("language", language);
        user.put("sourceToken", sourceToken);
        user.put("curSearchToken", curSearchToken);
        JSONObject getSource;
        try {
            getSource = Clients.WebServer.getSource(sourceToken);
            //todo will delete next line
            session.sendMessage(new TextMessage(getSource.toJSONString()));
        } catch (Exception e) {
            // todo exception controller
            return;
        }

        Response response = new Response("response", "init");
        response.setSourceToken(sourceToken);
        InitResponse init = new InitResponse();
        if (!"success".equals(getSource.getString("status"))) {
            init.setStatus("fail");
            init.setMessage("get sources from webserver fail.");
        } else {
            init.setStatus("success");
            init.setMessage("get sources from webserver success.");
            List<SourceReceived> srs = JSONArray.parseArray(getSource.getJSONArray("sources").toJSONString(), SourceReceived.class);
            List<RelationReceived> rrs = JSONArray.parseArray(JSONArray.toJSONString(getSource.getJSONArray("sources"),
                    new Common.JSONFilter()), RelationReceived.class);

            user.put("sources", srs);

            System.out.println(JSON.toJSONString(srs));

            //todo will delete next line
            session.sendMessage(new TextMessage(JSON.toJSONString(rrs)));
            for (SourceReceived sr : srs) {
                init.addSource(sr.transfer());
            }

            FocusParser fp = new FocusParser();
            ModelBuild.build(fp, srs);
            user.put("parser", fp);
        }
        response.setDatas(init.toJson());
        LoggerHandler.info(response.toString(), Constant.PRINT_LOG);
        session.sendMessage(new TextMessage(response.toString()));

    }

    private static void search(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        Response response = new Response("response", "search");
        JSONObject datas = new JSONObject();
        datas.put("status", "success");
        datas.put("message", "done");
        response.setDatas(datas);
        // response immediately
        session.sendMessage(new TextMessage(response.toString()));

        String search = params.getString("search");
        String event = params.getString("event");
        int position = params.getInteger("position");
        JSONObject biConfig = params.getJSONObject("biConfig");
        boolean debug = params.getBoolean("debug");
        List<String> queryFlags = JSONArray.parseArray(params.getString("queryFlags"), String.class);
        List<Ambiguities> ambiguities = JSONArray.parseArray(params.getString("ambiguities"), Ambiguities.class);

        FocusParser fp = (FocusParser) user.get("parser");
        String language = user.getString("language");
        List<SourceReceived> srs = JSONArray.parseArray(user.getJSONArray("sources").toJSONString(), SourceReceived.class);
        if (Constant.Event.FOCUS_IN.equalsIgnoreCase(event)) {
            focusIn(session, search, position, fp);
            return;
        }
        List<FocusToken> tokens = fp.focusAnalyzer.test(search, language);
        session.sendMessage(new TextMessage("split words:" + JSON.toJSONString(tokens)));

        for (FocusToken ft : tokens) {
            Set<String> ams = ft.getAmbiguities();
            if (ams != null && ams.size() > 1) {
                String amb = "ambiguity:\t" + ft.getStart() + "-" + ft.getEnd() + "," + ft.getWord() + "," + JSON.toJSONString(ams);
                System.out.println(amb);
                session.sendMessage(new TextMessage(amb));
                // todo 歧义处理
                return;
            }
        }

        try {
            FocusInst focusInst = fp.parse(tokens);
            session.sendMessage(new TextMessage(focusInst.toJSON().toJSONString()));

            String msg;
            if (focusInst.position < 0) {
                FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
                if (focusPhrase.isSuggestion()) {
                    int sug = 0;
                    while (sug < focusPhrase.size()) {
                        FocusNode tmpNode = focusPhrase.getNode(sug);
                        if (!tmpNode.isTerminal()) {
                            System.out.println("------------------------");
                            msg = "输入不完整:\n\t提示:" + tmpNode.getValue() + "\n";
                            System.out.println(msg);
                            session.sendMessage(new TextMessage(msg));


                        }
                        sug++;
                    }
                } else {
                    System.out.println("------------------------");
                    JSONObject json = InstructionBuild.build(focusInst, search, srs);
                    msg = "指令:\n\t" + json + "\n";
                    System.out.println(msg);
                    session.sendMessage(new TextMessage(msg));
                }
            } else {
                int tokenPosition = focusInst.position;
                int strPosition = tokens.get(focusInst.position).getStart();
                msg = "错误:\n\t" + "位置: " + strPosition + "\t错误: " + search.substring(strPosition) + "\n";
                System.out.println(msg);
                session.sendMessage(new TextMessage(msg));
                Set<String> sug = new HashSet<>();
                for (FocusPhrase focusPhrase : focusInst.getFocusPhrases()) {
                    if (!focusPhrase.isSuggestion()) {
                        tokenPosition = tokenPosition - focusPhrase.size();
                        continue;
                    }
                    sug.add("\n\t提示: " + focusPhrase.getNode(tokenPosition).getValue() + "\n");
                }
                System.out.println("------------------------");
                StringBuilder sb = new StringBuilder();
                sug.forEach(sb::append);
                msg = sb.toString();
                System.out.println(msg);
                session.sendMessage(new TextMessage(msg));
            }

        } catch (InvalidRuleException e) {
            e.printStackTrace();
        }

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

    private static void echo(WebSocketSession session, JSONObject user) throws IOException {
        JSONObject response = new JSONObject();
        response.put("type", "echo");
        session.sendMessage(new TextMessage(response.toJSONString()));
        FocusParser fp = (FocusParser) user.get("parser");
        String msg;
        if (fp != null) {
            msg = JSON.toJSONString(fp.getTerminalTokens());
        } else {
            msg = "null";
        }
        session.sendMessage(new TextMessage(msg));
    }

    // textChange
    private static void textChange() {

    }

    // focusIn
    private static void focusIn(WebSocketSession session, String question, int position, FocusParser fp) throws IOException {
        session.sendMessage(new TextMessage(JSON.toJSONString(fp.getTerminalTokens())));
    }

    // move
    private static void move() {

    }

}
