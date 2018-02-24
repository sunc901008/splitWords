package focus.search.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.core.Lexeme;
import focus.search.analyzer.focus.FocusKWDict;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.base.LoggerHandler;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.CommonFunc;
import focus.search.instruction.InstructionBuild;
import focus.search.meta.Column;
import focus.search.metaReceived.Ambiguities;
import focus.search.metaReceived.RelationReceived;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.search.*;
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
        LoggerHandler.info(response.toString(), Constant.PRINT_LOG);
        session.sendMessage(new TextMessage(response.toString()));

    }

    private static void search(WebSocketSession session, JSONObject params, JSONObject user) throws IOException {
        // response immediately
        session.sendMessage(new TextMessage(Response.response()));

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

        try {
            // 解析结果
            FocusInst focusInst = fp.parse(tokens);

//            session.sendMessage(new TextMessage(focusInst.toJSON().toJSONString()));

            String msg;
            if (focusInst.position < 0) {// 未出错
                // todo 检测歧义 [有bug,待改]
                int index = tokens.size();
                boolean over = false;
                SourceReceived preNodeSource = null;
                for (FocusPhrase focusPhrase : focusInst.getFocusPhrases()) {
                    for (int i = 0; i < focusPhrase.getFocusNodes().size(); i++) {
                        FocusNode fn = focusPhrase.getNode(i);
                        if (index == 0) {
                            over = true;
                            break;
                        }
                        index--;
                        String nodeType = fn.getType();
                        String value = fn.getValue();
                        if (nodeType.equals(Lexeme.INTEGER)
                                || nodeType.equals(Lexeme.NUMBER)
                                || nodeType.equals(Lexeme.SYMBOL)
                                || FocusKWDict.getAllKeywords().contains(value)) {
                            FocusNodeDetail focusNodeDetail = new FocusNodeDetail();
                            focusNodeDetail.type = nodeType;
                            focusNodeDetail.value = value;
                            fn.addDetail(focusNodeDetail);
                            continue;
                        }
                        SourceReceived source = CommonFunc.getSource(value, srs);
                        List<Column> columns = CommonFunc.getColumns(value, srs);


                        if (source != null) {// 有表名和当前值相同
                            FocusNodeDetail focusNodeDetail = new FocusNodeDetail();
                            focusNodeDetail.type = Constant.FNDType.TABLE;
                            focusNodeDetail.sourceId = source.tableId;
                            focusNodeDetail.sourceName = source.sourceName;
                            focusNodeDetail.value = value;
                            fn.addDetail(focusNodeDetail);
                        } else {

                        }

//                        if (source == null) {
//                            ColumnReceived col;
//                            if (preNodeSource != null && (col = CommonFunc.getCol(value, preNodeSource)) != null) {
//                                FocusNodeDetail focusNodeDetail = new FocusNodeDetail();
//                                focusNodeDetail.type = Constant.FNDType.COLUMN;
//                                focusNodeDetail.sourceId = preNodeSource.tableId;
//                                focusNodeDetail.sourceName = preNodeSource.sourceName;
//                                focusNodeDetail.columnId = col.columnId;
//                                focusNodeDetail.columnName = col.columnName;
//                                focusNodeDetail.colType = col.columnType;
//                                focusNodeDetail.dataType = col.dataType;
//                                focusNodeDetail.value = value;
//                                fn.addDetail(focusNodeDetail);
//                                continue;
//                            }
//                            List<SourceReceived> sources = CommonFunc.getSources(value, srs);
//                            if (sources.size() > 1) {// 有歧义
//                                AmbiguityResponse response = new AmbiguityResponse(search);
//                                AmbiguityResponse.Datas datas = new AmbiguityResponse.Datas();
//                                datas.id = UUID.randomUUID().toString();
//                                datas.begin = fn.getBegin();
//                                datas.end = fn.getEnd();
//                                datas.title = "ambiguity " + fn.getValue();
//                                over = true;
//                                break;
//                            }
//                        } else {
//                            preNodeSource = source;
//                            FocusNodeDetail focusNodeDetail = new FocusNodeDetail();
//                            focusNodeDetail.type = Constant.FNDType.TABLE;
//                            focusNodeDetail.sourceId = source.tableId;
//                            focusNodeDetail.sourceName = source.sourceName;
//                            focusNodeDetail.value = value;
//                            fn.addDetail(focusNodeDetail);
//                        }
                    }
                    if (over)
                        break;
                }

                FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
                if (focusPhrase.isSuggestion()) {// 出入不完整
                    SuggestionResponse response = new SuggestionResponse(search);
                    SuggestionResponse.Datas datas = new SuggestionResponse.Datas();
                    datas.beginPos = tokens.get(tokens.size() - 1).getEnd();
                    datas.phraseBeginPos = datas.beginPos;
                    sug(tokens.size() - 1, focusInst).forEach(s -> {
                        SuggestionResponse.Suggestions suggestion = new SuggestionResponse.Suggestions();
                        suggestion.suggestion = s;
                        suggestion.suggestionType = "aaa";
                        suggestion.description = "aaa";
                        datas.suggestions.add(suggestion);
                    });
                    response.setDatas(datas);
                    session.sendMessage(new TextMessage(response.response()));
                    System.out.println("提示:\n\t" + JSON.toJSONString(sug(tokens.size(), focusInst)) + "\n");
                } else {//  输入完整
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
                int strPosition = tokens.get(position).getStart();
                IllegalResponse.Datas datas = new IllegalResponse.Datas();
                datas.beginPos = strPosition;
                StringBuilder reason = new StringBuilder();
                sug(position, focusInst).forEach(s -> reason.append(s).append("\n"));
                datas.reason = reason.toString();
                response.setDatas(datas);
                session.sendMessage(new TextMessage(response.response()));
                msg = "错误:\n\t" + "位置: " + strPosition + "\t错误: " + search.substring(strPosition) + "\n";
                System.out.println(msg);
                msg = "提示:\n\t" + JSON.toJSONString(sug(position, focusInst)) + "\n";
                System.out.println(msg);
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

    private static Set<String> sug(int position, FocusInst focusInst) {
        Set<String> suggestions = new HashSet<>();
        if (position == 1)
            position = 0;
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            if (fp.isSuggestion()) {
                suggestions.add(fp.getNode(position).getValue());
            } else {
                position = position - fp.size();
            }
        }
        return suggestions;
    }

}
