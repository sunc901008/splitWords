package focus.search.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusToken;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusParser;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.instruction.InstructionBuild;
import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WsTest extends TextWebSocketHandler {
    private static final Logger logger = Logger.getLogger(WsTest.class);
    private static final ArrayList<WebSocketSession> users = new ArrayList<>();

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("someone connected to server.");
        users.add(session);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        users.remove(session);
        logger.info("someone disconnected to server.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException, InvalidRuleException {
        JSONObject params = JSON.parseObject(message.getPayload());
        String input = params.getString("input");
        parse(input, session);
    }

    private static void parse(String question, WebSocketSession session) throws IOException, InvalidRuleException {
        List<FocusToken> tokens = FocusAnalyzer.test(question, "english");

        String msg = "分词:\n\t" + JSON.toJSONString(tokens) + "\n";
        System.out.println(msg);
        session.sendMessage(new TextMessage(msg));

        List<TerminalToken> terminals = FocusParser.getTerminalTokens();
        msg = "最小单元词:\n\t" + JSON.toJSONString(terminals) + "\n";
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
                        String value = tmpNode.getValue();
                        msg = "提示:\n\t" + value + "\n";
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
            System.out.println("------------------------");
            msg = "错误:\n\t" + question.substring(focusInst.position) + "\n";
            System.out.println(msg);
            session.sendMessage(new TextMessage(msg));
            FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
            int sug = 0;
            if (focusPhrase != null)
                while (sug < focusPhrase.size()) {
                    FocusNode tmpNode = focusPhrase.getNode(sug);
                    if (!tmpNode.isTerminal()) {
                        System.out.println("------------------------");
                        msg = "应该输入:\n\t" + tmpNode.getValue() + "\n";
                        System.out.println(msg);
                        session.sendMessage(new TextMessage(msg));
                    }
                    sug++;
                }
        }
    }

}
