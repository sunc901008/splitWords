package focus.search.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.LoggerHandler;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusParser;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.InstructionBuild;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WsTest extends TextWebSocketHandler {
    private static final ArrayList<WebSocketSession> users = new ArrayList<>();

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        JSONObject user = (JSONObject) session.getAttributes().get("user");
        LoggerHandler.info(user.getString("name") + " connected to server.");
        users.add(session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        JSONObject user = (JSONObject) session.getAttributes().get("user");
        users.remove(session);
        LoggerHandler.info(user.getString("name") + " disconnected to server.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {/*
        String input = message.getPayload();
        if ("sources".equals(input)) {
            session.sendMessage(new TextMessage(JSON.toJSONString(DefaultModel.sources())));
            return;
        }
        try {
            parse(input, session);
        } catch (InvalidRuleException | IOException e) {
            FocusExceptionHandler.handle(session, e);
        }*/
    }
/*
    private static void parse(String question, WebSocketSession session) throws IOException, InvalidRuleException {
        List<FocusToken> tokens = FocusAnalyzer.test(question, "english");

        String msg = "分词:\n\t" + JSON.toJSONString(tokens) + "\n";
        System.out.println(msg);
        session.sendMessage(new TextMessage(msg));

        System.out.println("------------------------");
        FocusInst focusInst = FocusParser.parse(question, "english");
        msg = "解析:\n\t" + focusInst.toJSON().toJSONString() + "\n";
        System.out.println(msg);
        session.sendMessage(new TextMessage(msg));

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
                JSONObject json = InstructionBuild.build(focusInst, question);
                msg = "指令:\n\t" + json + "\n";
                System.out.println(msg);
                session.sendMessage(new TextMessage(msg));
            }
        } else {
            int tokenPosition = focusInst.position;
            int strPosition = tokens.get(focusInst.position).getStart();
            msg = "错误:\n\t" + "位置: " + strPosition + "\t错误: " + question.substring(strPosition) + "\n";
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
    }
*/
}
