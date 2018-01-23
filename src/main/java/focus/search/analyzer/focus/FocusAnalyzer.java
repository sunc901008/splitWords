package focus.search.analyzer.focus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.dic.Dictionary;
import focus.search.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class FocusAnalyzer {

    private static IKAnalyzer analyzer;

    public static void main(String[] args) throws IOException {

        String table1 = "{\"table\":\"USERS_ORACLE\", \"columns\":[\"id\",\"views\"]}";
/*
        JSONObject json = new JSONObject();
        json.put("type","init");
        System.out.println(json);

        json = new JSONObject();
        json.put("type","table");
        json.put("table", table1);
        System.out.println(json);

        json = new JSONObject();

        json.put("type","table");
        json.put("table", table2);
        System.out.println(json);

        json = new JSONObject();

        json.put("type","reset");
        System.out.println(json);

        json = new JSONObject();

        json.put("type","keyword");
        System.out.println(json);

        json = new JSONObject();

        json.put("type","split");
        json.put("input", "年龄的最大值>4部门");
        System.out.println(json);
*/

        init();

//        Dictionary.addWords(makeDict(JSON.parseObject(table1)));
//        Dictionary.addWords(makeDict(JSON.parseObject(table2)));
//
//        String str1 = "年龄的最大值>4部门";
//        List<FocusTokens> tokens = test(str1);
//        tokens.forEach(token -> System.out.println(token.toJson()));

//        Dictionary.reset();
//        Dictionary.addWords(MyDictionary.dictionaries);
//
//        Dictionary.addWords(makeDict(JSON.parseObject(table1)));
//
        List<FocusTokens> tokens = test("3+5", "english");
        System.out.println(JSON.toJSONString(tokens));

    }

    public static void init() {
        analyzer = new IKAnalyzer();
        Dictionary.addWords(FocusKWDict.dictionaries);
    }

    public static void addTable(JSONObject json) {
        if (analyzer == null)
            init();
        Dictionary.addWords(makeDict(json));
    }

    public static void reset() {
        Dictionary.reset();
        Dictionary.addWords(FocusKWDict.dictionaries);
    }

    public static List<FocusTokens> test(String str, String language) throws IOException {

        TokenStream ts = analyzer.tokenStream("focus", new StringReader(str));
        analyzer.loadSegmenters(language);
        // 获取词元位置属性
        OffsetAttribute offset = ts.addAttribute(OffsetAttribute.class);
        // 获取词元文本属性
        CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
        // 获取词元文本属性
        TypeAttribute type = ts.addAttribute(TypeAttribute.class);

        // 重置TokenStream（重置StringReader）
        ts.reset();
        // 迭代获取分词结果
        List<FocusTokens> tokens = new ArrayList<>();
        while (ts.incrementToken()) {
            FocusTokens token = new FocusTokens(term.toString(), type.type(), offset.startOffset(), offset.endOffset());
            analyzer.getSuggestions().forEach(token::addSuggestions);
            analyzer.getAmbiguity().forEach(token::addAmbiguities);
            tokens.add(token);
        }
        // 关闭TokenStream（关闭StringReader）
        ts.close();
        return tokens;
    }

    private static List<FocusKWDict> makeDict(JSONObject json) {
        String tblName = json.getString("table");
        JSONArray columns = json.getJSONArray("columns");
        List<FocusKWDict> list = new ArrayList<>();
        for (Object obj : columns) {
            FocusKWDict dict = new FocusKWDict(obj.toString(), "columnName", tblName);
            list.add(dict);
        }
        return list;
    }

}
