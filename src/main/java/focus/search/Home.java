package focus.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Constant;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.controller.common.SuggestionBuild;
import focus.search.instruction.InstructionBuild;
import focus.search.meta.Column;
import focus.search.meta.FormulaAnalysis;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.search.*;

import java.io.IOException;
import java.util.*;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class Home {

    public static void main(String[] args) throws IOException, InvalidRuleException {
//        String search = "badges name";

//        test(search);

//        formulaTest(makeFp());

        FocusParser parser = new FocusParser();
        List<SourceReceived> sourceReceiveds = ModelBuild.test(2);
        System.out.println(JSON.toJSONString(sourceReceiveds));
        ModelBuild.build(parser, sourceReceiveds);
        String ruleName = "<all-column>";
        List<TerminalToken> tokens = SuggestionBuild.terminalTokens(parser, ruleName);
        System.out.println(JSON.toJSONString(tokens));

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
        String search = "5+8*2";

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
        ModelBuild.build(parser, ModelBuild.test(2));

        List<FocusToken> tokens = parser.focusAnalyzer.test(search, "english");

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


        List<TerminalToken> ens = parser.getTerminalTokens();
        System.out.println(JSON.toJSONString(ens));

        System.out.println(JSON.toJSONString(tokens));
        FocusInst focusInst;
        try {
            focusInst = parser.parseFormula(tokens, new JSONObject());
        } catch (AmbiguitiesException e) {
            System.out.println("Ambiguity:");
            System.out.println(e.toString());
            return;
        }
        System.out.println("-------------------");
        System.out.println(focusInst.toJSON());

        if (ens.size() > 0) {
            return;
        }

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
                System.out.println("提示:\n\t" + JSON.toJSONString(response) + "\n");
            } else {//  输入完整

                JSONObject json = InstructionBuild.build(focusInst, search, new JSONObject());

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
            List<FocusNode> focusNodes = sug(focusInst.position, focusInst);
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


    private static Boolean isBaseRule(BnfRule rule, String token) {
        for (TerminalToken tt : rule.getTerminalTokens()) {
            if (tt.getName().toLowerCase().startsWith(token.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
