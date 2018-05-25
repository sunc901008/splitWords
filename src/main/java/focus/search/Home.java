package focus.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusKWDict;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.controller.common.Base;
import focus.search.controller.common.FormulaAnalysis;
import focus.search.controller.common.SuggestionBuild;
import focus.search.instruction.InstructionBuild;
import focus.search.instruction.annotations.AnnotationDatas;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.FocusParserException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.socket.TextMessage;

import java.io.BufferedReader;
import java.io.FileReader;
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

    public static void main(String[] args) throws IOException, InvalidRuleException, FocusInstructionException, FocusParserException, AmbiguitiesException, IllegalException {
        test("creationdate \"01/01/2000\" 之后", Constant.Language.CHINESE);
//        test("creationdate last 2 weeks", Constant.Language.ENGLISH);
//        test("creationdate last 2 months", Constant.Language.ENGLISH);
//        test("creationdate last 2 quarters", Constant.Language.ENGLISH);
//        test("creationdate last 2 years", Constant.Language.ENGLISH);

//        Calendar today = Calendar.getInstance();
//        print(today.get(Calendar.DAY_OF_WEEK_IN_MONTH));
//        print(today.get(Calendar.WEEK_OF_MONTH));
//        print(today.get(Calendar.WEEK_OF_YEAR));
//        print(today.get(Calendar.DAY_OF_WEEK));
//        print(Common.dateFormat("01/01/2011"));

//        String symbolValue = Constant.SymbolMapper.symbol.get("大于");
//        print(symbolValue);
//        te();

//        boolean expression = false;
//        search(0, 1, Constant.Language.CHINESE);
//        split(18, 1);
//        split(",>");
//        ttt();
//        formulaTest(makeFp());
//        test("if 4>3 then (if 5>2 then 3 else 5) else 6");
//        test("average(views+6)+2*(9-7) >5");
//        formula("5+2");

//        FocusInst fi = search("views+average(4)*2");
//        FocusPhrase fp = fi.lastFocusPhrase();
//        print(JSONObject.toJSONString(fp.allFormulaNode()));

//        print("abcd");
//        t();


//        String example = "diff_days ( %s )";
//        example = String.format(example, "%s , " + SuggestionBuild.dateSug());
//        print(example);

    }

    private static void te() {
        String str = "{\"ars\":[{\"realValue\":\"大于是的\",\"columnId\":0,\"type\":\"chinese\",\"possibleValue\":\"大于 是的\"},{\"realValue\":\"大于是的\",\"columnId\":0,\"type\":\"chinese\",\"possibleValue\":\"大于是 的\"}],\"end\":6,\"position\":0,\"begin\":2}";

        print(str);

        AmbiguitiesException ae = JSON.parseObject(str, AmbiguitiesException.class);

        print(ae.toJSON());


    }

    private static void t() throws IOException, FocusParserException, AmbiguitiesException {
//        FocusParser parser = new FocusParser();
//
//        List<FocusToken> tokens = parser.focusAnalyzer.test("co", "chinese");
//
//        System.out.println(JSON.toJSONString(tokens));
//        FocusInst focusInst = parser.parseQuestion(tokens, new JSONObject());
//        JSONObject json = SuggestionBuild.sug(tokens, focusInst);
//        print(json);
//        String str = json.getJSONArray("suggestions").toJSONString();
        AnnotationToken token = new AnnotationToken();
        token.value = "id";
        String str = "{\"ars\":[{\"columnId\":2,\"sourceName\":\"badges\",\"type\":\"column\",\"columnName\":\"id\"},{\"columnId\":7,\"sourceName\":\"users\",\"type\":\"column\",\"columnName\":\"id\"}],\"isResolved\":true,\"realValue\":\"id\"}";
        AmbiguitiesResolve tmp = JSONArray.parseObject(str, AmbiguitiesResolve.class);
        print("current ambiguities:" + tmp.toJSON());
        if (tmp.ars.size() > 1 && tmp.value.equalsIgnoreCase(token.value.toString())) {
            token.ambiguity = new AmbiguityDatas();
            token.ambiguity.begin = token.begin;
            token.ambiguity.end = token.end;
            token.ambiguity.title = "ambiguity word: " + token.value;
            tmp.ars.forEach(a -> token.ambiguity.possibleMenus.add(a.columnName + " in table " + a.sourceName));
        }
        print("TEST: token:" + token.toJSON());
    }

    private static void deepCloneTest() {
        FocusParser parser = Base.englishParser.deepClone();
        long start = Calendar.getInstance().getTimeInMillis();
        FocusParser parser1 = parser.deepClone();
        print(Calendar.getInstance().getTimeInMillis() - start);
        ModelBuild.buildTable(parser, ModelBuild.test(1));
        print(parser.getTerminalTokens().size());
        print(parser1.getTerminalTokens().size());
    }

    private static void allNumberFunc() {
        FocusParser fp = new FocusParser();
        BnfRule rule = fp.getRule("<no-number-function-column>");
        rule.getAlternatives().forEach(alt -> System.out.print("\"" + alt.get(0).getName() + "\","));
    }

    private static void ttt() {
        // annotation content
        AnnotationDatas datas = new AnnotationDatas();
        JSONArray instructions = new JSONArray();

        JSONObject json2 = new JSONObject();

        String in = "{\"args\": [{\"type\": \"number\",\"realValue\": 1},{\"type\": \"number\",\"realValue\": 1}],\"name\": \"+\",\"type\": \"function\"}";
        JSONObject instruction = JSONObject.parseObject(in);

        Formula formula = new Formula();
        formula.setId("asdfois");
        formula.setFormula("formula");
        formula.setInstruction(instruction);

        JSONObject json = new JSONObject();
        json.put("description", "formula description");
        json.put("formula", formula.toJSON());
        json.put("type", Constant.FNDType.FORMULA);
        json.put("detailType", Constant.FNDType.FORMULA);
        json.put("value", "formulaName");
        json.put("begin", 3);
        json.put("end", 23);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add("formulaName");
        json.put("tokens", jsonArray);
        datas.addToken(json);

        json2.put("content", datas);

        instructions.add(json2);

        JSONObject json1 = new JSONObject();
        json1.put("instId", Constant.InstIdType.ADD_EXPRESSION);
        json1.put("category", Constant.AnnotationCategory.EXPRESSION_OR_LOGICAL);
        String ss = formula.getInstruction().toJSONString();
        json1.put("expression", JSONObject.parse(ss));
        instructions.add(json1);

        in = instructions.toJSONString();
        print(in);
    }

    private static void split(String search) throws IOException, AmbiguitiesException {
        FocusAnalyzer focusAnalyzer = new FocusAnalyzer();
        List<FocusToken> tokens = focusAnalyzer.test(search, "chinese");
        print(JSONArray.toJSONString(tokens));
    }

    private static void split(int start, int length) throws IOException, AmbiguitiesException {
        ResourceLoader resolver = new DefaultResourceLoader();
        BufferedReader br = new BufferedReader(new FileReader(resolver.getResource("test/questions").getFile()));
        String search;
        String last = null;
        int i = 0;
        while ((search = br.readLine()) != null) {
            i++;
            if (search.startsWith("//"))
                continue;
            last = search;
            if (start == 0 || i >= start) {
                split(search);
                System.out.println();
                length--;
            }
            if (length == 0) {
                break;
            }
        }
        if (start < 0) {
            split(last);
        }

        br.close();
    }

    // params:  start 需要执行的questions文件中的起始行号，为0时执行所有, length 执行的行数
    private static void search(int start, int length) throws IOException, InvalidRuleException, FocusInstructionException,
            FocusParserException, AmbiguitiesException, IllegalException {
        search(start, length, Constant.Language.ENGLISH);
    }

    private static void search(int start, int length, String language) throws IOException, InvalidRuleException, FocusInstructionException, FocusParserException, AmbiguitiesException, IllegalException {
        String file;
        if (Constant.Language.ENGLISH.equals(language)) {
            file = "test/questions";
        } else {
            file = "test/cquestions";
        }
        ResourceLoader resolver = new DefaultResourceLoader();
        BufferedReader br = new BufferedReader(new FileReader(resolver.getResource(file).getFile()));
        String search;
        String last = null;
        int i = 0;
        while ((search = br.readLine()) != null) {
            i++;
            last = search;
            if (search.startsWith("//") || start < 0 || search.isEmpty())
                continue;
            if (start == 0 || i >= start) {
                print(i);
                long begin = Calendar.getInstance().getTime().getTime();
                test(search, language);
                long end = Calendar.getInstance().getTime().getTime();
                print(end - begin);
                System.out.println();
                length--;
            }
            if (length == 0 && start != 0) {
                break;
            }
        }
        if (start < 0) {
            test(last, language);
        }

        br.close();
    }

    private static void formula(String search) throws IOException, FocusParserException, FocusInstructionException, AmbiguitiesException, IllegalException {
        FocusInst fi = search(search);
        if (fi != null && fi.size() == 1) {
            FocusPhrase fp = fi.lastFocusPhrase();
            FormulaAnalysis.FormulaObj formulaObj = FormulaAnalysis.numberAnalysis(fp);
            System.out.println(fp.toJSON());
            System.out.println(formulaObj.toString());
        }
    }

    private static void print(Object object) {
        System.out.println(object);
    }

    private static void focusPhraseTest() {
        FocusPhrase fp0 = new FocusPhrase();
        fp0.setInstName("parent0");

        FocusNode parentFn00 = new FocusNode("parentFn00");
        FocusPhrase fp1 = new FocusPhrase();
        fp1.setInstName("parent1");

        FocusNode parentFn10 = new FocusNode("parentFn10");

        FocusPhrase fp2 = new FocusPhrase();
        fp1.setInstName("parent2");
        FocusNode parentFn20 = new FocusNode("parentFn20");
        FocusNode parentFn21 = new FocusNode("parentFn21");
        fp2.addPn(parentFn20);
        fp2.addPn(parentFn21);

        parentFn10.setChildren(fp2);

        FocusNode parentFn11 = new FocusNode("parentFn11");

        fp1.addPn(parentFn10);
        fp1.addPn(parentFn11);

        parentFn00.setChildren(fp1);

        FocusNode parentFn01 = new FocusNode("parentFn01");
        fp0.addPn(parentFn00);
        fp0.addPn(parentFn01);

        print(fp0.toJSON());

        int test = 1;

        print(JSONArray.toJSONString(fp0.allNode()));

        print(fp0.getNodeNew(test).toJSON());

        print("--------");
        FocusNode replace = new FocusNode("replace");

        fp0.replaceNode(test, replace);

        print(fp0.toJSON());

        print(JSONArray.toJSONString(fp0.allNode()));
        print(fp0.getNodeNew(test).toJSON());
    }

    private static FocusPhrase makeFp() {
        FocusPhrase fp = new FocusPhrase();
        FocusNode fn1 = new FocusNode();
        fn1.setType("integer");
        fn1.setValue("5");
        FocusNode fn2 = new FocusNode();
        fn2.setType("symbol");
        fn2.setValue("*");
        FocusNode fn3 = new FocusNode();
        fn3.setType("integer");
        fn3.setValue("8");
        FocusNode fn4 = new FocusNode();
        fn4.setType("symbol");
        fn4.setValue("+");
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

    private static void split(String search, String language) throws IOException {
        FocusAnalyzer focusAnalyzer = new FocusAnalyzer();
        List<FocusKWDict> fks = new ArrayList<>();
//        fks.add(new FocusKWDict("大于是的", Constant.FNDType.KEYWORD));
//        fks.add(new FocusKWDict("大于是", Constant.FNDType.KEYWORD));
//        fks.add(new FocusKWDict("的", Constant.FNDType.KEYWORD));
//        fks.add(new FocusKWDict("是的", Constant.FNDType.KEYWORD));
//        fks.add(new FocusKWDict("的确", Constant.FNDType.KEYWORD));
        focusAnalyzer.testInit(fks);
        try {
            List<FocusToken> tokens = focusAnalyzer.test(search, language);
            print(JSON.toJSONString(tokens));
        } catch (AmbiguitiesException e) {
            print("*********");
            print(e.toString());
        }

    }

    private static FocusPhrase make() {
        String str = "{\"focusPhrases\":[{\"focusNodes\":[{\"isTerminal\":true,\"end\":6,\"type\":\"keyword\",\"value\":\"strlen\",\"begin\":0}," +
                "{\"isTerminal\":true,\"end\":7,\"type\":\"keyword\",\"value\":\"(\",\"begin\":6},{\"isTerminal\":true,\"end\":8," +
                "\"type\":\"keyword\",\"value\":\"\\\"\",\"begin\":7},{\"isTerminal\":true,\"end\":13,\"type\":\"columnValue\"," +
                "\"value\":\"focus\",\"begin\":8},{\"isTerminal\":true,\"end\":14,\"type\":\"keyword\",\"value\":\"\\\"\",\"begin\":13}," +
                "{\"isTerminal\":true,\"end\":15,\"type\":\"keyword\",\"value\":\")\",\"begin\":14},{\"isTerminal\":true,\"end\":17," +
                "\"type\":\"symbol\",\"value\":\">\",\"begin\":16},{\"isTerminal\":true,\"end\":19,\"type\":\"integer\",\"value\":\"5\"," +
                "\"begin\":18}],\"instName\":\"<filter>\",\"type\":\"instruction\"}],\"position\":-1}";
        FocusInst focusInst = JSONObject.parseObject(str, FocusInst.class);
        return focusInst.getFocusPhrases().get(0);
    }

    private static void formulaTest(FocusPhrase fp) throws FocusInstructionException, IllegalException, AmbiguitiesException {

        FormulaAnalysis.FormulaObj formulaObj = FormulaAnalysis.analysis(fp);

        System.out.println(fp.toJSON());
        System.out.println(formulaObj.toString());

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

    private static FocusInst search(String search) throws IOException, FocusParserException, AmbiguitiesException {
        return search(search, Constant.Language.ENGLISH);
    }

    private static FocusInst search(String search, String language) throws IOException, FocusParserException, AmbiguitiesException {
        String test = "bnf-file/test.bnf";
        FocusParser parser = new FocusParser(language);
//        FocusParser parser = new FocusParser(test);
        ModelBuild.buildTable(parser, ModelBuild.test(1));

//        ModelBuild.buildFormulas(parser, Collections.singletonList(search));

        List<FocusToken> tokens = parser.focusAnalyzer.test(search, language);

        System.out.println(JSON.toJSONString(tokens));
        try {
            FocusInst focusInst = parser.parseQuestion(tokens, new JSONObject());
            System.out.println("-------------------");
            System.out.println(focusInst.toJSON());
            System.out.println("-------------------");
            String q = search;
            boolean over = false;
            if (focusInst.position >= 0) {
                over = true;
                q = q + "  |  " + focusInst.position + ":" + tokens.get(focusInst.position).getWord();
            }
            System.out.println(q);

            if (over) {
                return null;
            }
            return focusInst;

        } catch (AmbiguitiesException e) {
            System.out.println("Ambiguity:");
            System.out.println(e.toString());
        }
        return null;
    }

    private static void test(String search) throws IOException, FocusInstructionException, FocusParserException, AmbiguitiesException, IllegalException {
        test(search, Constant.Language.ENGLISH);
    }

    private static void test(String search, String language) throws IOException, FocusInstructionException, FocusParserException, IllegalException, AmbiguitiesException {

//        FocusInst focusInst = search(search, language);

        FocusParser parser = new FocusParser(language);
//        FocusParser parser = new FocusParser(test);
        ModelBuild.buildTable(parser, ModelBuild.test(1));

//        ModelBuild.buildFormulas(parser, Collections.singletonList(search));

        List<FocusToken> tokens = parser.focusAnalyzer.test(search, language);

        print(JSON.toJSONString(tokens));
        FocusInst focusInst = parser.parseQuestion(tokens, new JSONObject());


        if (focusInst == null) {
            return;
        }
        print(focusInst.toJSON());

        if (focusInst.position < 0) {
            FocusPhrase focusPhrase = focusInst.lastFocusPhrase();
            if (focusPhrase.isSuggestion()) {// 出入不完整
                SuggestionResponse response = new SuggestionResponse(search);
                SuggestionDatas datas = new SuggestionDatas();
                JSONObject json = SuggestionBuild.sug(tokens, focusInst);
                print("Get Suggestions:" + json);
                datas.beginPos = json.getInteger("position");
                datas.phraseBeginPos = datas.beginPos;
                List<FocusNode> focusNodes = JSONArray.parseArray(json.getJSONArray("suggestions").toJSONString(), FocusNode.class);
                focusNodes.forEach(node -> datas.suggestions.addAll(SuggestionBuild.buildSug(parser, null, node)));
                response.setDatas(datas);

                print("提示:\n\t" + JSON.toJSONString(focusNodes) + "\n");
            } else {
                JSONObject json = null;
                try {
                    json = InstructionBuild.build(focusInst, search, new JSONObject(), new ArrayList<>());
                } catch (AmbiguitiesException e) {
                        AmbiguityResponse response = new AmbiguityResponse(search);

                        String title;
                        if (e.position < 0) {
                            title = search.substring(e.begin, e.end);
                        } else {
                            title = tokens.get(e.position).getWord();
                        }

                        AmbiguityDatas datas = new AmbiguityDatas();
                        datas.begin = e.begin;
                        datas.end = e.end;
                        datas.id = "id";
                        datas.title = "ambiguity word: " + title;
                        e.ars.forEach(a -> datas.possibleMenus.add(a.columnName + " in table " + a.sourceName));
                        response.setDatas(datas);
                        print(response.response());

                }

                print("指令:\n\t" + json + "\n");

                // Annotations
                JSONArray instructions = json.getJSONArray("instructions");
                print(instructions);
            }
        } else {
            print("error!");
            print(focusInst.position);
        }

    }

}
