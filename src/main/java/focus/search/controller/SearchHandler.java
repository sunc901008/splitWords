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
import focus.search.meta.AmbiguitiesRecord;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.metaReceived.Ambiguities;
import focus.search.metaReceived.RelationReceived;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.search.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

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
        // response immediately
        session.sendMessage(new TextMessage(Response.response(type)));
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
                disambiguate(session, datas, user);
                break;
            case "reDisambiguate":
                reDisambiguate(session, datas, user);
                break;
            case "clearDisambiguate":
                clearDisambiguate(session, datas, user);
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

        // 歧义记录
        JSONObject ambiguities = new JSONObject();
        user.put("ambiguities", ambiguities);

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

        InitResponse response = new InitResponse("response", "init");
        response.setSourceToken(sourceToken);
        InitResponse.Init init = new InitResponse.Init();
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
        LoggerHandler.info(response.response(), Constant.PRINT_LOG);
        session.sendMessage(new TextMessage(response.response()));

    }

    private static void search(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        // response immediately
//        session.sendMessage(new TextMessage(Response.response("search")));

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
        // 分词
        List<FocusToken> tokens = fp.focusAnalyzer.test(search, language);
        System.out.println("split words:" + JSON.toJSONString(tokens));

        JSONObject amb = user.getJSONObject("ambiguities");

        try {
            // 解析结果
            FocusInst focusInst = fp.parse(tokens, amb);

            System.out.println(focusInst.toJSON().toJSONString());

            String msg;
            if (focusInst.position < 0) {// 未出错
                FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
                if (focusPhrase.isSuggestion()) {// 出入不完整
                    SuggestionResponse response = new SuggestionResponse(search);
                    SuggestionResponse.Datas datas = new SuggestionResponse.Datas();
                    JSONObject json = sug(tokens, focusInst);
                    datas.beginPos = json.getInteger("position");
                    datas.phraseBeginPos = datas.beginPos;
                    List<FocusNode> focusNodes = JSONArray.parseArray(json.getJSONArray("suggestions").toJSONString(), FocusNode.class);
                    focusNodes.forEach(node -> {
                        SuggestionResponse.Suggestions suggestion = new SuggestionResponse.Suggestions();
                        suggestion.suggestion = node.getValue();
                        suggestion.suggestionType = node.getType();
                        if (Constant.FNDType.TABLE.equalsIgnoreCase(node.getType())) {
                            suggestion.description = "this is a table name";
                        } else if (Constant.FNDType.COLUMN.equalsIgnoreCase(node.getType())) {
                            Column col = node.getColumn();
                            suggestion.description = "column '" + node.getValue() + "' in table '" + col.getSourceName() + "'";
                        }
                        datas.suggestions.add(suggestion);
                    });
                    response.setDatas(datas);
                    session.sendMessage(new TextMessage(response.response()));
                    System.out.println("提示:\n\t" + JSON.toJSONString(focusNodes) + "\n");
                } else {//  输入完整
                    // Annotations
                    AnnotationResponse annotationResponse = new AnnotationResponse(search);
                    session.sendMessage(new TextMessage(annotationResponse.response()));

                    StateResponse response = new StateResponse(search);
                    // 生成指令
                    response.setDatas("prepareQuery");
                    session.sendMessage(new TextMessage(response.response()));
                    JSONObject json = InstructionBuild.build(focusInst, search);
                    System.out.println("指令:\n\t" + json + "\n");
                    // 指令检测
                    response.setDatas("precheck");
                    session.sendMessage(new TextMessage(response.response()));
                    // todo 发送BI检测指定是否可执行

                    // 指令检测完毕
                    response.setDatas("precheckDone");
                    session.sendMessage(new TextMessage(response.response()));

                    // 准备执行指令
                    response.setDatas("executeQuery");
                    session.sendMessage(new TextMessage(response.response()));

                    // todo 给BI发送指令，获取查询结果
                    ChartsResponse chartsResponse = new ChartsResponse(search, user.getString("sourceToken"));
                    chartsResponse.setDatas(null);
                    session.sendMessage(new TextMessage(chartsResponse.response()));

                }
            } else {//  出错
                IllegalResponse response = new IllegalResponse(search);
                int strPosition = tokens.get(focusInst.position).getStart();
                IllegalResponse.Datas datas = new IllegalResponse.Datas();
                datas.beginPos = strPosition;
                StringBuilder reason = new StringBuilder();
                List<FocusNode> focusNodes = sug(focusInst.position, focusInst);
                focusNodes.forEach(node -> {
                    reason.append(node.getValue()).append(",").append(node.getType()).append(",").append(node.getColumn().getColumnId());
                    reason.append("|");
                });
                datas.reason = reason.toString();
                response.setDatas(datas);
                session.sendMessage(new TextMessage(response.response()));
                msg = "错误:\n\t" + "位置: " + strPosition + "\t错误: " + search.substring(strPosition) + "\n";
                System.out.println(msg);
                msg = "提示:\n\t" + reason + "\n";
                System.out.println(msg);
            }

        } catch (InvalidRuleException e) {
            e.printStackTrace();
        } catch (AmbiguitiesException e) {
            AmbiguityResponse response = new AmbiguityResponse(search);
            FocusToken ft = tokens.get(e.position);
            AmbiguityResponse.Datas datas = new AmbiguityResponse.Datas();
            datas.begin = ft.getStart();
            datas.end = ft.getEnd();
            datas.id = UUID.randomUUID().toString();
            datas.title = "ambiguity word: " + ft.getWord();
            e.ars.forEach(a -> datas.possibleMenus.add(a.columnName + " in table " + a.sourceName));
            response.setDatas(datas);
            session.sendMessage(new TextMessage(response.response()));
            System.out.println(response.response());

            AmbiguitiesResolve ar = new AmbiguitiesResolve();
            ar.ars = e.ars;
            ar.value = ft.getWord();
            amb.put(datas.id, ar);
            user.put("ambiguities", amb);

            session.getAttributes().put("user", user);

        }
    }

    private static void selectSuggest(WebSocketSession session, JSONObject params) throws IOException {

    }

    private static void disambiguate(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        String id = params.getString("id");
        int index = params.getInteger("index");
        JSONObject amb = user.getJSONObject("ambiguities");
        AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getById(id, amb);
        AmbiguitiesRecord ar = ambiguitiesResolve.ars.remove(index);
        ambiguitiesResolve.ars.add(0, ar);
        ambiguitiesResolve.isResolved = true;
        amb.put(id, ambiguitiesResolve);
        user.put("ambiguities", amb);
        session.getAttributes().put("user", user);
        JSONObject response = new JSONObject();
        response.put("type", "state");
        response.put("message", "disambiguateDone");
        session.sendMessage(new TextMessage(response.toJSONString()));

    }

    private static void reDisambiguate(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        disambiguate(session, params, user);
    }

    private static void clearDisambiguate(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        user.put("ambiguities", new JSONObject());
        session.getAttributes().put("user", user);
        JSONObject response = new JSONObject();
        response.put("type", "state");
        response.put("message", "clearDisambiguateDone");
        session.sendMessage(new TextMessage(response.toJSONString()));
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

    // suggestions| 出错
    private static List<FocusNode> sug(int position, FocusInst focusInst) {
        List<FocusNode> focusNodes = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            if (fp.isSuggestion()) {
                FocusNode fn = fp.getNode(position);
                if (!suggestions.contains(fn.getValue())) {
                    suggestions.add(fn.getValue());
                    focusNodes.add(fn);
                }
            } else {
                position = position - fp.size();
            }
        }
        return focusNodes;
    }

    // suggestions| 输入不完整
    private static JSONObject sug(List<FocusToken> tokens, FocusInst focusInst) {
        JSONObject json = new JSONObject();
        int index = tokens.size() - 1;
        int position = tokens.get(index).getStart();
        List<FocusNode> focusNodes = new ArrayList<>();
        Set<String> suggestions = new HashSet<>();
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            if (fp.isSuggestion()) {
                FocusNode fn = fp.getNode(index);
                if (fn.getValue().equalsIgnoreCase(tokens.get(index).getWord())) {
                    FocusNode focusNode = fp.getNode(index + 1);
                    if (!suggestions.contains(focusNode.getValue())) {
                        suggestions.add(focusNode.getValue());
                        focusNodes.add(focusNode);
                        position = fn.getEnd() + 1;
                    }
                } else {
                    if (!suggestions.contains(fn.getValue())) {
                        suggestions.add(fn.getValue());
                        focusNodes.add(fn);
                    }
                }
            } else {
                index = index - fp.size();
            }
        }
        json.put("position", position);
        json.put("suggestions", focusNodes);
        return json;
    }

}
