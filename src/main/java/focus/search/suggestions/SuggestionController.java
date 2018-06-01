package focus.search.suggestions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.*;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.Token;
import focus.search.bnf.tokens.TokenString;
import focus.search.meta.Column;
import focus.search.meta.HistoryQuestion;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.search.SuggestionDatas;
import focus.search.response.search.SuggestionResponse;
import focus.search.response.search.SuggestionSuggestion;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * creator: sunc
 * date: 2018/3/23
 * description:
 */
public class SuggestionController {
    private static final Logger logger = Logger.getLogger(SuggestionController.class);
    private static final List<String> randomString = Arrays.asList("hello", "world", "focus", "example");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 预设提示规则，规则内互斥，规则外互不影响
    private static final List<String> DEFAULT_DATE_BNF_1 = Arrays.asList("<growth-of>");
    private static final List<String> DEFAULT_DATE_BNF_2 = Arrays.asList("<last-filter>", "<before-after-filter>");

    private static final List<List<String>> DEFAULT_BNF = Arrays.asList(DEFAULT_DATE_BNF_1, DEFAULT_DATE_BNF_2);

    /**
     * ① 输入的question能够匹配上一个完整的bnf规则，即能够正常下发指令。
     * <p>
     * 方案： 映射到输入框为空时的情况给出 suggestion
     *
     * @param fp        解析类
     * @param search    需要解析的question
     * @param focusInst 解析结果
     * @param user      websocket用户信息
     * @param tokens    分词结果
     * @return SuggestionResponse
     */
    public static SuggestionResponse suggestions(final FocusParser fp, String search, FocusInst focusInst, JSONObject user, List<FocusToken> tokens, int position) {
        if (tokens.get(tokens.size() - 1).getEnd() > position) {//光标在search中间
            // TODO: 2018/5/31 当光标在search中间的时候，给出suggestion
            return null;
        }
        boolean addSpace = tokens.get(tokens.size() - 1).getEnd() == position;//根据光标位置判断是否需要在suggestion前面添加一个空格
        SuggestionResponse response = new SuggestionResponse(search);
        SuggestionDatas datas = new SuggestionDatas();

        datas.beginPos = focusInst.lastFocusPhrase().getLastNode().getEnd();
        datas.phraseBeginPos = datas.beginPos;

        JSONArray historyQuestions = user.getJSONArray("historyQuestions");

        List<List<String>> bnfList = checkDefault(focusInst);

        for (Object history : historyQuestions) {
            String suggestion = ((HistoryQuestion) history).question;
            if (suggestion.startsWith(search.trim())) {
                SuggestionSuggestion ss = new SuggestionSuggestion();
                ss.suggestion = suggestion;
                ss.suggestionType = "history";
                ss.description = "history";
                datas.suggestions.add(ss);
            }
        }

        for (List<String> bnfName : bnfList) {
            for (String ruleName : bnfName) {
                String suggestion = terminalToken(fp, ruleName);
                suggestion = addSpace ? " " + suggestion : suggestion;
                SuggestionSuggestion ss = new SuggestionSuggestion();
                ss.suggestion = suggestion;
                ss.suggestionType = "suggestion system";
                ss.description = "suggestion system";
                datas.suggestions.add(ss);
            }
        }
        response.setDatas(datas);
        return response;
    }

    // suggestions| 出错
    public static List<FocusNode> sug(int position, FocusInst focusInst) {
        List<FocusNode> focusNodes = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            if (fp.isSuggestion()) {
                FocusNode fn = fp.getNodeNew(position);
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
    // todo 修改提示 按规则名提示规则内容
    public static JSONObject sug(List<FocusToken> tokens, FocusInst focusInst) {
        JSONObject json = new JSONObject();
        int index = tokens.size() - 1;
        int position = tokens.get(index).getStart();
        List<JSONObject> focusNodes = new ArrayList<>();
        Set<String> suggestions = new HashSet<>();
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            if (fp.isSuggestion()) {
                FocusNode fn = fp.getNodeNew(index);
                if (fn.getValue().equalsIgnoreCase(tokens.get(index).getWord())) {
                    FocusNode focusNode = fp.getNodeNew(index + 1);
                    if (!suggestions.contains(focusNode.getValue())) {
                        suggestions.add(focusNode.getValue());
                        focusNodes.add(focusNode.toJSON());
                        position = fn.getEnd() + 1;
                    }
                } else {
                    if (!suggestions.contains(fn.getValue())) {
                        suggestions.add(fn.getValue());
                        focusNodes.add(fn.toJSON());
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

    // 根据bnf规则名字给出一条完整的提示
    private static String terminalToken(FocusParser parser, String ruleName) {
        String sug = "";
        BnfRule br = parser.getRule(ruleName);
        if (br != null) {
            for (Token token : br.getAlternatives().get(0)) {
                if (token instanceof TerminalToken) {
                    if (!((TerminalToken) token).getType().equals(Constant.FNDType.TABLE)) {
                        sug = sug + token.getName() + " ";
                    }
                } else {
                    return sug + terminalToken(parser, token.getName());
                }
            }
        }
        return sug;
    }

    // 根据bnf规则名字获取最底层的单元token
    public static List<TerminalToken> terminalTokens(FocusParser parser, String ruleName) {
        List<TerminalToken> tokens = new ArrayList<>();
        BnfRule br = parser.getRule(ruleName);
        if (br != null)
            for (TokenString ts : br.getAlternatives()) {
                for (Token token : ts) {
                    if (token instanceof TerminalToken) {
                        if (!((TerminalToken) token).getType().equals(Constant.FNDType.TABLE) && !isExist(tokens, (TerminalToken) token)) {
                            tokens.add((TerminalToken) token);
                        }
                    } else {
                        for (TerminalToken t : terminalTokens(parser, token.getName())) {
                            if (!isExist(tokens, t)) {
                                tokens.add(t);
                            }
                        }
                    }
                }
            }
        return tokens;
    }

    private static boolean isExist(List<TerminalToken> tokens, TerminalToken token) {
        for (TerminalToken t : tokens) {
            if (t.getType().equals(token.getType()) && t.getName().equals(token.getName())) {
                if (token.getType().equals(Constant.FNDType.COLUMN)) {
                    if (token.getColumn().getColumnId() == t.getColumn().getColumnId()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    // 随机获取几个列信息
    public static List<Column> colRandomSuggestions(JSONObject user) {
        return colRandomSuggestions(user, "");
    }

    // 随机获取几个指定类型的列信息
    public static List<Column> colRandomSuggestions(JSONObject user, String type) {
        List<SourceReceived> srs = JSONArray.parseArray(user.getJSONArray("sources").toJSONString(), SourceReceived.class);
        List<Column> columns = new ArrayList<>();
        for (SourceReceived source : srs) {
            for (ColumnReceived col : source.columns) {
                if (Common.isEmpty(type) || col.dataType.equals(type)) {
                    Column column = col.transfer();
                    column.setSourceName(source.sourceName);
                    column.setPhysicalName(source.physicalName);
                    column.setTableId(source.tableId);
                    columns.add(column);
                }
            }
        }
        return columns;
    }

    // 随机获取几个指定类型的列信息
    public static List<Column> colRandomSuggestions(JSONObject user, List<String> types) {
        List<SourceReceived> srs = JSONArray.parseArray(user.getJSONArray("sources").toJSONString(), SourceReceived.class);
        List<Column> columns = new ArrayList<>();
        int count = 10;
        for (SourceReceived source : srs) {
            if (count <= 0) {
                break;
            }
            for (ColumnReceived col : source.columns) {
                if (types.contains(col.dataType)) {
                    Column column = col.transfer();
                    column.setSourceName(source.sourceName);
                    column.setPhysicalName(source.physicalName);
                    column.setTableId(source.tableId);
                    columns.add(column);
                    count--;
                }
            }
        }
        return columns;
    }

    /**
     * 获取数字类型的提示
     *
     * @param isInt 是否为整数
     * @return 数字提示
     */
    public static String decimalSug(boolean isInt) {
        Double d = Math.random() * 100 + 1;
        return isInt ? String.valueOf(d.intValue()) : Common.decimalFormat(d);
    }

    public static String decimalSug() {
        return decimalSug(true);
    }

    public static int decimalSug(int max) {
        Double d = Math.random() * max;
        return d.intValue();
    }

    /**
     * @return 随机的一个字符串单词, 带双引号
     * @see #randomString {@link #randomString}
     */
    public static String stringSug() {
        return String.format("\"%s\"", randomString.get(decimalSug(randomString.size())));
    }

    /**
     * @return 随机的一个时间字符串, 带双引号
     * @see #sdf {@link #sdf}
     */
    public static String dateSug() {
        Date date = Calendar.getInstance().getTime();
        return String.format("\"%s\"", sdf.format(date));
    }

    /**
     * @param fp   解析树
     * @param node 节点信息
     * @return 根据 node 返回 suggestion
     */
    public static List<SuggestionSuggestion> buildSug(FocusParser fp, JSONObject user, FocusNode node) {
        logger.debug("current node:" + node.toJSON());
        List<SuggestionSuggestion> suggestions = new ArrayList<>();
        if (node.isTerminal()) {
            SuggestionSuggestion suggestion = new SuggestionSuggestion();
            suggestion.suggestion = node.getValue();
            suggestion.suggestionType = node.getType();
            if (Constant.FNDType.TABLE.equalsIgnoreCase(node.getType())) {
                suggestion.description = "this is a table name";
            } else if (Constant.FNDType.COLUMN.equalsIgnoreCase(node.getType())) {
                Column col = node.getColumn();
                suggestion.description = "column '" + node.getValue() + "' in table '" + col.getSourceName() + "'";
            } else if (Constant.FNDType.INTEGER.equalsIgnoreCase(node.getType())) {
                suggestion.description = "this is a integer";
                suggestion.suggestion = decimalSug();
            } else if (Constant.FNDType.DOUBLE.equalsIgnoreCase(node.getType())) {
                suggestion.description = "this is a double";
                suggestion.suggestion = decimalSug(false);
            } else if (Constant.FNDType.SYMBOL.equalsIgnoreCase(node.getType())) {
                suggestion.description = "this is a symbol";
            } else if (Constant.FNDType.KEYWORD.equalsIgnoreCase(node.getType())) {
                suggestion.description = "this is a keyword";
            } else if (Constant.FNDType.COLUMNVALUE.equalsIgnoreCase(node.getType())) {
                suggestion.description = "this is a column value";
            }
            suggestions.add(suggestion);
            return suggestions;
        } else {
            switch (node.getValue()) {
                case "<number>":
                    SuggestionSuggestion suggestion1 = new SuggestionSuggestion();
                    suggestion1.suggestion = decimalSug();
                    suggestion1.suggestionType = Constant.FNDType.INTEGER;
                    suggestion1.description = "this is a integer";
                    suggestions.add(suggestion1);

                    SuggestionSuggestion suggestion2 = new SuggestionSuggestion();
                    suggestion2.suggestion = decimalSug(false);
                    suggestion2.suggestionType = Constant.FNDType.DOUBLE;
                    suggestion2.description = "this is a double";
                    suggestions.add(suggestion2);
                    return suggestions;
                case "<all-columns>":
                    List<Column> allColumns = colRandomSuggestions(user, Constant.DataType.INT);
                    if (allColumns.size() > 0) {
                        Double allDouble = Math.random() * allColumns.size();
                        suggestions.add(colSug(allColumns.get(allDouble.intValue())));
                    }
                    List<Column> stringColumns = colRandomSuggestions(user, Constant.DataType.STRING);
                    if (stringColumns.size() > 0) {
                        Double stringDouble = Math.random() * stringColumns.size();
                        suggestions.add(colSug(stringColumns.get(stringDouble.intValue())));
                    }
                    return suggestions;
                case "<number-columns>":
                    List<Column> intColumns = colRandomSuggestions(user, Constant.DataType.INT);
                    if (intColumns.size() > 0) {
                        Double intDouble = Math.random() * intColumns.size();
                        suggestions.add(colSug(intColumns.get(intDouble.intValue())));
                    }
                    List<Column> doubleColumns = colRandomSuggestions(user, Constant.DataType.DOUBLE);
                    if (doubleColumns.size() > 0) {
                        Double doubleDouble = Math.random() * doubleColumns.size();
                        suggestions.add(colSug(doubleColumns.get(doubleDouble.intValue())));
                    }
                    return suggestions;
                case "<string-columns>":
                    List<Column> stringColumns1 = colRandomSuggestions(user, Constant.DataType.STRING);
                    if (stringColumns1.size() > 0) {
                        Double stringDouble1 = Math.random() * stringColumns1.size();
                        suggestions.add(colSug(stringColumns1.get(stringDouble1.intValue())));
                    }
                    return suggestions;
                case "<bool-columns>":
                    List<Column> boolColumns = colRandomSuggestions(user, Constant.DataType.BOOLEAN);
                    if (boolColumns.size() > 0) {
                        Double boolDouble = Math.random() * boolColumns.size();
                        suggestions.add(colSug(boolColumns.get(boolDouble.intValue())));
                    }
                    return suggestions;
                case "<date-columns>":
                    List<Column> dateColumns = colRandomSuggestions(user, Constant.DataType.TIMESTAMP);
                    if (dateColumns.size() > 0) {
                        Double dateDouble = Math.random() * dateColumns.size();
                        suggestions.add(colSug(dateColumns.get(dateDouble.intValue())));
                    }
                    return suggestions;
                case "<growth-of-measure>":
                    List<Column> intColumns1 = colRandomSuggestions(user, Constant.DataType.INT);
                    if (intColumns1.size() > 0) {
                        Double intDouble1 = Math.random() * intColumns1.size();
                        suggestions.add(colSug(intColumns1.get(intDouble1.intValue())));
                    }
                    List<TerminalToken> tokens = fp.getRule("<growth-of-measure-operation>").getTerminalTokens();
                    Double d = Math.random() * tokens.size();
                    suggestions.add(keywordSug(tokens.get(d.intValue()).getName()));
                    return suggestions;
                case "<growth-of-by-date-interval>":
                    List<TerminalToken> tokens1 = fp.getRule("<growth-of-by-date-interval>").getTerminalTokens();
                    Double d1 = Math.random() * tokens1.size();
                    suggestions.add(keywordSug(tokens1.get(d1.intValue()).getName()));
                    return suggestions;
                case "<year-over-year>":
                    suggestions.add(keywordSug("year over year"));
                    return suggestions;
                case "<bool-symbol>":
                    List<TerminalToken> tokens2 = fp.getRule("<bool-symbol>").getTerminalTokens();
                    Double d2 = Math.random() * tokens2.size();
                    suggestions.add(keywordSug(tokens2.get(d2.intValue()).getName()));
                    return suggestions;
                case "<math-symbol>":
                    List<TerminalToken> tokens3 = fp.getRule("<math-symbol>").getTerminalTokens();
                    Double d3 = Math.random() * tokens3.size();
                    suggestions.add(keywordSug(tokens3.get(d3.intValue()).getName()));
                    return suggestions;
                default:
                    return suggestions;
            }
        }
    }

    public static SuggestionSuggestion colSug(Column col) {
        SuggestionSuggestion suggestion = new SuggestionSuggestion();
        suggestion.suggestion = col.getColumnDisplayName();
        suggestion.suggestionType = Constant.FNDType.COLUMN;
        suggestion.description = "column '" + col.getColumnDisplayName() + "' in table '" + col.getSourceName() + "'";
        return suggestion;
    }

    public static SuggestionSuggestion keywordSug(String keyword) {
        SuggestionSuggestion suggestion = new SuggestionSuggestion();
        suggestion.suggestion = keyword;
        suggestion.suggestionType = Constant.FNDType.KEYWORD;
        suggestion.description = "this is a keyword";
        return suggestion;
    }

    /**
     * 检测当前解析结果中是否含有预设的规则，返回预设的规则列表
     *
     * @return 可以提示的规则列表
     */
    public static List<List<String>> checkDefault(FocusInst focusInst) {
        List<List<String>> bnfList = new ArrayList<>();
        bnfList.addAll(DEFAULT_BNF);
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            List<String> defaultBnf = checkDefault(fp, 5);
            if (defaultBnf != null) {
                bnfList.remove(defaultBnf);
            }
        }
        return bnfList;
    }

    private static List<String> checkDefault(FocusPhrase fp, int deep) {
        for (List<String> defaultBnf : DEFAULT_BNF) {
            if (defaultBnf.contains(fp.getInstName())) {
                return defaultBnf;
            }
        }
        if (deep > 0) {
            for (FocusNode fn : fp.getFocusNodes()) {
                List<String> tmp = checkDefault(fn.getChildren(), deep--);
                if (tmp != null) {
                    return tmp;
                }
            }
        }
        return null;
    }

}