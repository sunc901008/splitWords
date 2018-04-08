package focus.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Constant;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.controller.common.FormulaAnalysis;
import focus.search.controller.common.SuggestionBuild;
import focus.search.instruction.InstructionBuild;
import focus.search.meta.Column;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class Home {

    public static void main(String[] args) throws IOException, InvalidRuleException {

        FocusAnalyzer focusAnalyzer = new FocusAnalyzer();

//        String search = "5+8*2";
        String search = "count ( d";
//        String search = "views > 5 1+5";

//        List<FocusToken> tokens = focusAnalyzer.test(search, "english");
//
//        System.out.println(JSON.toJSONString(tokens));
//
//        tokens = MergeToken.mergeUserInput(tokens, search);
//
//        System.out.println(JSON.toJSONString(tokens));

//
        test(search);

//        formulaTest(makeFp());

//        FocusParser parser = new FocusParser();
//        List<SourceReceived> sourceReceiveds = ModelBuild.test(2);
//        System.out.println(JSON.toJSONString(sourceReceiveds));
//        ModelBuild.build(parser, sourceReceiveds);
//        String ruleName = "<symbol>";
//        List<TerminalToken> tokens = SuggestionBuild.terminalTokens(parser, ruleName);
//        System.out.println(JSON.toJSONString(tokens));
//
//        String name = "abcd>";
//
//        for (TerminalToken token : tokens) {
//            System.out.print(token.getName());
//            if (name.toLowerCase().contains(token.getName())) {
//                System.out.println("conflictWithKeyword");
//            }
//            System.out.println();
//        }
//        System.out.println("over");

    }

    private static FocusPhrase makeFp() {
        FocusPhrase fp = new FocusPhrase();
        FocusNode fn1 = new FocusNode();
        fn1.setType("integer");
        fn1.setValue("5");
        FocusNode fn2 = new FocusNode();
        fn2.setType("symbol");
        fn2.setValue("+");
        FocusNode fn3 = new FocusNode();
        fn3.setType("integer");
        fn3.setValue("8");
        FocusNode fn4 = new FocusNode();
        fn4.setType("symbol");
        fn4.setValue("*");
        FocusNode fn5 = new FocusNode();
        fn5.setType("integer");
        fn5.setValue("2");
        fp.addPn(fn1);
        fp.addPn(fn2);
        fp.addPn(fn3);
        fp.addPn(fn4);
        fp.addPn(fn5);
        return fp;
    }

    private static void formulaTest(FocusPhrase fp) {
        long received = Calendar.getInstance().getTimeInMillis();
        System.out.println(received);
        String search = "to";

        FormulaResponse response = new FormulaResponse(search);
        received = Calendar.getInstance().getTimeInMillis();
        System.out.println(received);

        FormulaAnalysis.FormulaObj formulaObj = FormulaAnalysis.analysis(fp);
        received = Calendar.getInstance().getTimeInMillis();
        System.out.println(received);

        System.out.println(fp.toJSON());
        System.out.println(formulaObj.toString());

        FormulaResponse.Datas datas = new FormulaResponse.Datas();
        datas.settings = FormulaAnalysis.getSettings(formulaObj);
        datas.formulaObj = formulaObj.toString();
        response.setDatas(datas);
        received = Calendar.getInstance().getTimeInMillis();
        System.out.println(received);

        System.out.println(response.response());
        System.out.println(SearchFinishedResponse.response(search, received));
        received = Calendar.getInstance().getTimeInMillis();
        System.out.println(received);

    }

    private static JSONObject sug() {
        JSONObject json = new JSONObject();
        List<FocusNode> focusNodes = new ArrayList<>();
        FocusNode focusNode = new FocusNode();
        focusNode.setType("table");
        focusNodes.add(focusNode);
        FocusNode focusNode1 = new FocusNode();
        focusNode1.setType("column");
        Column column = new Column();
        column.setSourceName("users");
        column.setColumnId(1);
        focusNode1.setColumn(column);
        focusNodes.add(focusNode1);
        json.put("suggestions", focusNodes);
        return json;
    }

    private static void test(String search) throws IOException, InvalidRuleException {

        FocusParser parser = new FocusParser();
        ModelBuild.buildTable(parser, ModelBuild.test(2));

//        ModelBuild.buildFormulas(parser, Collections.singletonList(search));

        List<FocusToken> tokens = parser.focusAnalyzer.test(search, "chinese");

//        List<String> keywords = FocusKWDict.getAllKeywords();
//        int loop = tokens.size();
//        while (loop > 0) {
//            FocusToken ft = tokens.remove(0);
//            if (keywords.contains(ft.getWord())) {
//                ft.setType("keyword");
//            }
//            tokens.add(ft);
//            loop--;
//        }


        System.out.println(JSON.toJSONString(tokens));
        FocusInst focusInst;
        try {
            focusInst = parser.parseQuestion(tokens, new JSONObject());
        } catch (AmbiguitiesException e) {
            System.out.println("Ambiguity:");
            System.out.println(e.toString());
            return;
        }
        System.out.println("-------------------");
        System.out.println(focusInst.toJSON());
        if (tokens.size() > 0) {
            return;
        }

        String msg;
        if (focusInst.position < 0) {// 未出错
            FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
            if (focusPhrase.isSuggestion()) {// 出入不完整
                SuggestionResponse response = new SuggestionResponse(search);
                SuggestionResponse.Datas datas = new SuggestionResponse.Datas();
                JSONObject json = SuggestionBuild.sug(tokens, focusInst);
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
                System.out.println("提示:\n\t" + JSON.toJSONString(response) + "\n");
            } else {//  输入完整

                JSONObject json = InstructionBuild.build(focusInst, search, new JSONObject(), new ArrayList<>());

                System.out.println("指令:\n\t" + json + "\n");

                // Annotations
                AnnotationResponse annotationResponse = new AnnotationResponse(search);
                JSONArray instructions = json.getJSONArray("instructions");
                for (int i = 0; i < instructions.size(); i++) {
                    JSONObject instruction = instructions.getJSONObject(i);
                    if (instruction.getString("instId").equals("annotation")) {
                        String content = instruction.getString("content");
                        annotationResponse.datas.add(JSONObject.parseObject(content, AnnotationResponse.Datas.class));
                    }
                }
                System.out.println(annotationResponse.response());

            }
        } else {//  出错
            IllegalResponse response = new IllegalResponse(search);
            int strPosition = tokens.get(focusInst.position).getStart();
            IllegalResponse.Datas datas = new IllegalResponse.Datas();
            datas.beginPos = strPosition;
            StringBuilder reason = new StringBuilder();
            List<FocusNode> focusNodes = SuggestionBuild.sug(focusInst.position, focusInst);
            focusNodes.forEach(node -> {
                reason.append(node.getValue());
                if (node.getType() != null) {
                    reason.append(",").append(node.getType());
                }
                if (node.getColumn() != null) {
                    reason.append(",").append(node.getColumn().getColumnId());
                }
                reason.append("\r\n");
            });
            datas.reason = reason.toString();
            response.setDatas(datas);
            msg = "错误:\n\t" + "位置: " + strPosition + "\t错误: " + search.substring(strPosition) + "\n";
            System.out.println(msg);
            msg = "提示:\n\t" + reason + "\n";
            System.out.println(msg);
        }

    }

    private static Boolean isBaseRule(BnfRule rule, String token) {
        for (TerminalToken tt : rule.getTerminalTokens()) {
            if (tt.getName().toLowerCase().startsWith(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
