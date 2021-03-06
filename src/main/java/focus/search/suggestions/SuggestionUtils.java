package focus.search.suggestions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.base.LanguageUtils;
import focus.search.bnf.*;
import focus.search.bnf.tokens.*;
import focus.search.controller.common.Base;
import focus.search.controller.common.FormulaAnalysis;
import focus.search.meta.Column;
import focus.search.meta.HistoryQuestion;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusHttpException;
import focus.search.response.exception.FocusParserException;
import focus.search.response.exception.IllegalException;
import focus.search.response.search.IllegalDatas;
import focus.search.response.search.SuggestionDatas;
import focus.search.response.search.SuggestionResponse;
import focus.search.response.search.SuggestionSuggestion;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * creator: sunc
 * date: 2018/5/30
 * description: suggestion utils
 */
public class SuggestionUtils {
    private static final Logger logger = Logger.getLogger(SuggestionUtils.class);

    // 预设提示规则，规则内互斥，规则外互不影响
    private static final List<String> DEFAULT_DATE_BNF_1 = Arrays.asList("<growth-of>");// todo add other suggestion
    private static final List<String> DEFAULT_DATE_BNF_2 = Arrays.asList("<last-filter>", "<before-after-filter>");

    private static final List<List<String>> DEFAULT_BNF = Arrays.asList(DEFAULT_DATE_BNF_1, DEFAULT_DATE_BNF_2);

    private static final Integer DEFAULT_BNF_DEEP = 5;

    private static final int historySize = 3;

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
    public static SuggestionResponse suggestionsCompleted(final FocusParser fp, String search, FocusInst focusInst, JSONObject user, List<FocusToken> tokens, int position, JSONArray columnIdList) throws IllegalException, IOException, AmbiguitiesException {
        String language = user.getString("language");
        if (tokens.get(tokens.size() - 1).getEnd() > position) {//光标在search中间
            return middlePosition(fp, search, user, tokens, position);
        }
        boolean addSpace = tokens.get(tokens.size() - 1).getEnd() == position;//根据光标位置判断是否需要在suggestion前面添加一个空格
        SuggestionResponse response = new SuggestionResponse(search);
        SuggestionDatas datas = new SuggestionDatas();

//        datas.beginPos = focusInst.firstFocusPhrase().getLastNode().getEnd();
//        datas.phraseBeginPos = datas.beginPos;

        JSONArray historyQuestions = user.getJSONArray("historyQuestions");

        List<List<String>> bnfList = checkDefault(focusInst);

        String historyDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_history);

        boolean isQuestion = Constant.CategoryType.QUESTION.equalsIgnoreCase(user.getString("category"));
        if (isQuestion) {
            int count = 0;
            for (Object history : historyQuestions) {
                if (count >= historySize) {
                    break;
                }
                String suggestion = ((HistoryQuestion) history).question;
                String tokensToStr = FocusToken.tokensToString(tokens);
                if (suggestion.startsWith(tokensToStr) && !suggestion.equals(tokensToStr)) {
                    SuggestionSuggestion ss = new SuggestionSuggestion();
                    ss.beginPos = 0;
                    ss.endPos = position;
                    ss.suggestion = suggestion;
                    ss.suggestionType = Constant.SuggestionType.HISTORY;
                    ss.description = historyDescription;
                    datas.addSug(ss);
                    count++;
                }
            }
        }
        completed(fp, datas, focusInst, tokens, position, addSpace, language, columnIdList);

        if (isQuestion) {
            String systemDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_system);
            for (List<String> bnfName : bnfList) {
                for (String ruleName : bnfName) {
                    BnfRule br = fp.getRule(ruleName);
                    String suggestion = bnfRuleSug(fp, br, columnIdList).trim();
                    if (suggestion.isEmpty())
                        continue;
                    suggestion = addSpace ? " " + suggestion : suggestion;
                    SuggestionSuggestion ss = new SuggestionSuggestion();
                    ss.beginPos = position;
                    ss.endPos = position;
                    ss.suggestion = suggestion;
                    ss.suggestionType = Constant.SuggestionType.PHRASE;
                    ss.description = systemDescription;
                    datas.addSug(ss);
                }
            }
        }
        response.setDatas(datas);
        return response;
    }

    private static void completed(final FocusParser fp, SuggestionDatas datas, FocusInst focusInst, List<FocusToken> tokens, int position, boolean addSpace, String language, JSONArray columnIdList) throws IllegalException {
        completedOrNot(fp, datas, focusInst, tokens, position, addSpace, true, language, columnIdList);
    }

    /**
     * ② 输入的question能够匹配上bnf规则，但是不完整
     * <p>
     * 方案： 基于能够适配当前输入的question的bnf给出 suggestion
     *
     * @param fp        解析类
     * @param search    需要解析的question
     * @param focusInst 解析结果
     * @param user      websocket用户信息
     * @param tokens    分词结果
     * @return SuggestionResponse
     */
    public static SuggestionResponse suggestionsNotCompleted(final FocusParser fp, String search, FocusInst focusInst, JSONObject user, List<FocusToken> tokens, int position, JSONArray columnIdList) throws IllegalException, IOException,
            AmbiguitiesException {
        String language = user.getString("language");
        if (tokens.get(tokens.size() - 1).getEnd() > position) {//光标在search中间
            return middlePosition(fp, search, user, tokens, position);
        }
        boolean addSpace = tokens.get(tokens.size() - 1).getEnd() == position;//根据光标位置判断是否需要在suggestion前面添加一个空格
        SuggestionResponse response = new SuggestionResponse(search);
        SuggestionDatas datas = new SuggestionDatas();

//        datas.beginPos = focusInst.firstFocusPhrase().getLastNode().getEnd();
//        datas.phraseBeginPos = datas.beginPos;

        JSONArray historyQuestions = user.getJSONArray("historyQuestions");
        String historyDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_history);

        boolean isQuestion = Constant.CategoryType.QUESTION.equalsIgnoreCase(user.getString("category"));
        if (isQuestion) {
            int count = 0;
            for (Object history : historyQuestions) {
                if (count >= historySize) {
                    break;
                }
                String suggestion = ((HistoryQuestion) history).question;
                String tokensToStr = FocusToken.tokensToString(tokens);
                if (suggestion.startsWith(tokensToStr)) {
                    SuggestionSuggestion ss = new SuggestionSuggestion();
                    ss.beginPos = 0;
                    ss.endPos = position;
                    ss.suggestion = suggestion;
                    ss.suggestionType = Constant.SuggestionType.HISTORY;
                    ss.description = historyDescription;
                    datas.addSug(ss);
                    count++;
                }
            }
        }
        completedOrNot(fp, datas, focusInst, tokens, position, addSpace, false, language, columnIdList);

        response.setDatas(datas);
        return response;
    }

    private static void completedOrNot(final FocusParser fp, SuggestionDatas datas, FocusInst focusInst, List<FocusToken> tokens, int position, boolean addSpace, boolean completed, String language, JSONArray columnIdList) throws IllegalException {
        String columnDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_column);
        String formulaDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_formula);

        int index = tokens.size() - 1;
        int last = tokens.size() - 1;
        Set<String> suggestions = new HashSet<>();//已经添加的suggestion
        List<Integer> hasAddColumnIds = new ArrayList<>();//已经添加的列ID
        List<SuggestionSuggestion> sss = new ArrayList<>(); // 非补全的提示，优先级低于补全的提示(如:输入"age>",suggestion里面 ">=" 优先级高于 "10")
        List<String> ruleNames = new ArrayList<>(); // 记录已经提示过的规则名，规则的多种写法只提示一种
        for (FocusPhrase focusPhrase : focusInst.getFocusPhrases()) {
            if (focusPhrase.isSuggestion()) {
                FocusNode fn = focusPhrase.getNodeNew(index);
                if (fn.getValue().equalsIgnoreCase(tokens.get(last).getWord())) {
                    if (focusPhrase.size() <= index + 1) {
                        continue;
                    }
                    if (Constant.FNDType.DATE_VALUE.equals(fn.getType()) && Common.isEmpty(Common.dateFormat(fn.getValue()))) {// 日期字符串并且非法
//                        datas.guidance = String.format("%SExample: %s.", DATE_GUIDANCE, SourcesUtils.dateSug());
//                        break;
                        String reason = "invalid date format";
                        IllegalDatas illegalDatas = new IllegalDatas(fn.getBegin(), fn.getEnd(), reason);
                        throw new IllegalException(reason, illegalDatas);
                    }
                    if (Constant.FNDType.COLUMN_VALUE.equals(fn.getType())) {// 列中值
                        if ("<string-simple-filter>".equals(focusPhrase.getNode(0).getChildren().getInstName())) {
                            FocusNode colNode = focusPhrase.getFirstNode();
                            if (Constant.FNDType.TABLE.equals(colNode.getType())) {
                                colNode = focusPhrase.getNodeNew(1);
                            }
                            Column column = colNode.getColumn();
                            String word = fn.getValue();

                            JSONObject result;
                            try {
                                result = Clients.Index.tokens(column, word, 10);
                            } catch (FocusHttpException e) {
                                logger.warn("Get tokens from index fail!!!");
                                logger.error(Common.printStacktrace(e));
                                break;
                            }
                            JSONArray jsonArray = result.getJSONArray("tokens");
                            if (!jsonArray.isEmpty()) {// 有列中值，只提示列中值
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JSONObject token = jsonArray.getJSONObject(i);
                                    SuggestionSuggestion ss = new SuggestionSuggestion();
                                    ss.beginPos = focusPhrase.getNodeNew(index - 1).getBegin();
                                    ss.endPos = position;
                                    ss.suggestion = String.format("\"%s\"", token.getString("content"));
                                    ss.suggestionType = Constant.SuggestionType.COLUMN_VALUE;
                                    ss.description = String.format(columnDescription, column.getSourceName());
                                    sss.add(ss);
                                }
                                break;
                            } else {// 没有列中值，提示其他
                                logger.warn(String.format("column value '%s' is not exist. column:id:%d,name:%s", word, column.getColumnId(), column.getColumnDisplayName()));
                            }
                        }
                    }

                    String stringGuidance = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_string_guidance);
                    String dateGuidance = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_date_guidance);
                    String numberDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_number);

                    FocusNode focusNode = focusPhrase.getNodeNew(index + 1);
                    String inputValue = focusNode.getValue();
                    if (ColumnValueTerminalToken.COLUMN_VALUE.equals(inputValue) || ColumnValueTerminalToken.COLUMN_VALUE_BNF.equals(inputValue)) {
                        datas.guidance = String.format(stringGuidance, "\"focus\",'data'.");
                        break;
                    }
                    if (DateValueTerminalToken.DATE_VALUE.equals(inputValue) || DateValueTerminalToken.DATE_VALUE_BNF.equals(inputValue)) {
                        datas.guidance = String.format(dateGuidance, SourcesUtils.dateSug());
                        break;
                    }
                    if (!focusNode.isTerminal()) {
                        BnfRule br = fp.getRule(focusNode.getValue());
                        if (br == null || suggestions.contains(br.getLeftHandSide().getName())) {
                            continue;
                        }
                        List<TerminalToken> terminalTokens = terminalToken(fp, br);
                        while (!terminalTokens.isEmpty()) {
                            TerminalToken token = terminalTokens.remove(0);
                            String value = token.getName();
                            String type = token.getType();
                            if (suggestions.contains(value) && !Constant.FNDType.COLUMN.equals(type)) {
                                continue;
                            }
                            suggestions.add(br.getLeftHandSide().getName());
                            suggestions.add(value);
                            if (Constant.FNDType.INTEGER.equals(type) || Constant.FNDType.DOUBLE.equals(type)) {
                                value = SourcesUtils.decimalSug(true);
                                String suggestion = addSpace ? " " + value : value;
                                SuggestionSuggestion ss = new SuggestionSuggestion();
                                ss.beginPos = position;
                                ss.endPos = position;
                                ss.suggestion = suggestion;
                                ss.suggestionType = Constant.SuggestionType.NUMBER;
                                ss.description = numberDescription;
                                sss.add(ss);
                            } else if (Constant.FNDType.COLUMN.equals(type)) {
                                Column column = token.getColumn();
                                if (!columnIdList.contains(column.getColumnId())) {
                                    continue;
                                }
                                String suggestion = addSpace ? " " + value : value;
                                SuggestionSuggestion ss = new SuggestionSuggestion();
                                ss.beginPos = position;
                                ss.endPos = position;
                                ss.suggestion = suggestion;
                                ss.suggestionType = Constant.SuggestionType.COLUMN;
                                ss.description = String.format(columnDescription, column.getSourceName());
                                sss.add(ss);
                            } else if (Constant.FNDType.FORMULA.equals(type)) {
                                SuggestionSuggestion ssTmp = new SuggestionSuggestion();
                                ssTmp.beginPos = position;
                                ssTmp.endPos = position;
                                ssTmp.suggestion = addSpace ? " " + value : value;
                                ssTmp.suggestionType = Constant.SuggestionType.FORMULA;
                                ssTmp.description = formulaDescription;
                                sss.add(ssTmp);
                            } else {
                                String suggestion = addSpace ? " " + value : value;
                                SuggestionSuggestion ss = new SuggestionSuggestion();
                                ss.beginPos = position;
                                ss.endPos = position;
                                ss.suggestion = suggestion;
                                ss.suggestionType = type;
                                ss.description = type;
                                sss.add(ss);
                            }
                        }
                    } else {
                        if (!suggestions.contains(focusNode.getValue())) {
                            suggestions.add(focusNode.getValue());
                            String suggestion = addSpace ? " " + focusNode.getValue() : focusNode.getValue();
                            SuggestionSuggestion ss = new SuggestionSuggestion();
                            ss.beginPos = position;
                            ss.endPos = position;
                            ss.suggestion = suggestion;
                            ss.suggestionType = focusNode.getType();
                            String description = ss.suggestionType;
                            if (Constant.FNDType.COLUMN.equals(ss.suggestionType)) {
                                Column column = focusNode.getColumn();
                                description = String.format(columnDescription, column.getSourceName());
                            }
                            ss.description = description;
                            sss.add(ss);
                        }
                    }
                } else {
                    String ruleName = terminalRule(fp, fn.getValue());
                    if (ruleName == null) {
                        continue;
                    }
                    if (!ruleName.startsWith("<table-") && !ruleName.endsWith("-column>") && !ruleName.endsWith("-function>")) {//表名，列名，方法名
                        if (ruleNames.contains(ruleName)) {
                            continue;
                        }
                        ruleNames.add(ruleName);
                    }
                    if (suggestions.contains(fn.getValue()) && !Constant.SuggestionType.COLUMN.equals(fn.getType())) {
                        continue;
                    }
                    SuggestionSuggestion ss = new SuggestionSuggestion();
                    ss.beginPos = tokens.get(last).getStart();
                    ss.endPos = position;
                    ss.suggestion = fn.getValue();//不相等的时候不需要额外做添加空格的处理
                    ss.suggestionType = fn.getType();
                    ss.description = ss.suggestionType;
                    if (Constant.SuggestionType.COLUMN.equals(fn.getType())) {
                        Column column = fn.getColumn();
                        if (hasAddColumnIds.contains(column.getColumnId())) {//该列已经添加
                            continue;
                        }
                        hasAddColumnIds.add(column.getColumnId());
                        ss.description = String.format(columnDescription, column.getColumnDisplayName());
                        datas.addSug(ss, suggestions.contains(fn.getValue()));
                    } else {
                        datas.addSug(ss);
                    }
                    suggestions.add(fn.getValue());
                }
            } else {
                index = index - focusPhrase.size();
                if (index < 0) {
                    index = index + focusPhrase.size();
                    if (completed) {
                        ruleNames.clear();
                        FocusNode tmpNode = focusPhrase.getNodeNew(index);
                        if (Constant.FNDType.INTEGER.equals(tmpNode.getType()) || Constant.FNDType.DOUBLE.equals(tmpNode.getType())) {
                            continue;
                        }
                        String val = tmpNode.getValue();
                        ruleNames.add(terminalRule(fp, val));
                    }
                }
            }
        }
        datas.addAllSug(sss);
    }

    private static String terminalRule(final FocusParser fp, String value) {
        try {
            String ruleName = fp.parse(value).getLeftHandSide().getName();
            if (!ruleName.endsWith("-symbol>")) {
                return ruleName;
            }
        } catch (FocusParserException e) {
            logger.info(Common.printStacktrace(e));
        }
        return null;
    }

    /**
     * ③ 出错位置在 question 中间位置
     * 1. 光标在未出错部分的search之后的时候,提示出错的部分
     * <p>
     * 方案： 基于能够适配当前输入的question出错位置之前的输入的bnf给出 suggestion
     *
     * @param fp        解析类
     * @param search    需要解析的question
     * @param focusInst 解析结果
     * @param user      websocket用户信息
     * @param tokens    分词结果
     */
    public static List<SuggestionSuggestion> suggestionsMiddleError(final FocusParser fp, String search, FocusInst focusInst, JSONObject user, List<FocusToken> tokens, JSONArray columnIdList) throws IllegalException, IOException, AmbiguitiesException {
        String language = user.getString("language");
        int errorTokenIndex = focusInst.position;
        int beginPos = tokens.get(errorTokenIndex).getStart();
        int endPos = search.length();
        tokens = tokens.subList(0, errorTokenIndex);
        List<SuggestionSuggestion> sss = new ArrayList<>();

        JSONArray historyQuestions = user.getJSONArray("historyQuestions");
        String historyDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_history);

        boolean isQuestion = Constant.CategoryType.QUESTION.equalsIgnoreCase(user.getString("category"));
        if (isQuestion) {
            int count = 0;
            for (Object history : historyQuestions) {
                if (count >= historySize) {
                    break;
                }
                String suggestion = ((HistoryQuestion) history).question;
                String tokensToStr = FocusToken.tokensToString(tokens);
                if (suggestion.startsWith(tokensToStr)) {
                    SuggestionSuggestion ss = new SuggestionSuggestion();
                    ss.beginPos = 0;
                    ss.endPos = endPos;
                    ss.suggestion = suggestion;
                    ss.suggestionType = Constant.SuggestionType.HISTORY;
                    ss.description = historyDescription;
                    sss.add(ss);
                    count++;
                }
            }
        }
        Set<String> suggestions = new HashSet<>();
        String columnDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_column);
        String numberDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_number);
        String colValueDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_column_value);
        String dateValueDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_date_value);

        for (FocusPhrase focusPhrase : focusInst.getFocusPhrases()) {
            if (errorTokenIndex <= 0) {
                break;
            }
            if (focusPhrase.isSuggestion()) {
                FocusNode focusNode = focusPhrase.getNodeNew(errorTokenIndex);
                if (!focusNode.isTerminal()) {
                    BnfRule br = fp.getRule(focusNode.getValue());
                    List<TerminalToken> terminalTokens = terminalToken(fp, br);
                    boolean hasNumber = false;
                    for (TerminalToken token : terminalTokens) {
                        if (suggestions.contains(token.getName())) {
                            continue;
                        }
                        suggestions.add(token.getName());
                        String value = token.getName();
                        String type = token.getType();
                        String description = type;
                        if (Constant.FNDType.INTEGER.equals(token.getType()) || Constant.FNDType.DOUBLE.equals(token.getType())) {
                            if (hasNumber) {
                                continue;
                            } else {
                                hasNumber = true;
                            }
                            value = SourcesUtils.decimalSug(true);
                            description = numberDescription;
                        }

                        String val = focusNode.getValue();
                        if (ColumnValueTerminalToken.COLUMN_VALUE.equals(val) || ColumnValueTerminalToken.COLUMN_VALUE_BNF.equals(val)) {
                            value = SourcesUtils.stringSug();
                            type = Constant.SuggestionType.COLUMN_VALUE;
                            description = colValueDescription;
                        } else if (DateValueTerminalToken.DATE_VALUE.equals(val) || DateValueTerminalToken.DATE_VALUE_BNF.equals(val)) {
                            value = SourcesUtils.dateSug();
                            type = Constant.SuggestionType.DATE_VALUE;
                            description = dateValueDescription;
                        }

                        if (Constant.FNDType.COLUMN.equals(type)) {
                            Column column = token.getColumn();
                            if (!columnIdList.contains(column.getColumnId())) {
                                continue;
                            }
                            description = String.format(columnDescription, column.getColumnDisplayName());
                        }

                        SuggestionSuggestion ss = new SuggestionSuggestion();
                        ss.beginPos = beginPos;
                        ss.endPos = endPos;
                        ss.suggestion = value;
                        ss.suggestionType = type;
                        ss.description = description;
                        sss.add(ss);
                    }
                } else {
                    if (!suggestions.contains(focusNode.getValue())) {
                        suggestions.add(focusNode.getValue());
                        SuggestionSuggestion ss = new SuggestionSuggestion();
                        ss.beginPos = beginPos;
                        ss.endPos = endPos;
                        ss.suggestion = focusNode.getValue();
                        ss.suggestionType = focusNode.getType();
                        String description = ss.suggestionType;
                        if (Constant.FNDType.COLUMN.equals(ss.suggestionType)) {
                            Column column = focusNode.getColumn();
                            description = String.format(columnDescription, column.getColumnDisplayName());
                        }
                        ss.description = description;
                        sss.add(ss);
                    }
                }
            } else {
                errorTokenIndex = errorTokenIndex - focusPhrase.size();
            }
        }
        return sss;
    }

    /**
     * ③ 出错位置在 question 中间位置
     * 2. 光标在未出错部分的search中间的时候,给出提示
     * <p>
     * 方案： 基于能够适配当前输入的question出错位置之前的输入的bnf给出 suggestion
     *
     * @param fp     解析类
     * @param search 需要解析的question
     * @param user   websocket用户信息
     */
    public static SuggestionResponse suggestionsMiddleError(final FocusParser fp, String search, JSONObject user, List<FocusToken> tokens, int position) throws IllegalException, IOException, AmbiguitiesException {
        return middlePosition(fp, search, user, tokens, position);
    }

    /**
     * ④ 出错位置在 question 开始位置
     * <p>
     * 方案： 完全等同输入框为空时的情况给出 suggestion
     *
     * @param fp     解析类
     * @param search 需要解析的question
     * @param user   websocket用户信息
     */
    public static void suggestionsStartError(final FocusParser fp, String search, JSONObject user, IllegalDatas datas) {
        SuggestionResponse suggestionResponse = suggestionsNull(fp, user, search, search.length());
        datas.suggestions = suggestionResponse.getDatas().suggestions;
        datas.reason = "can not understand";
    }

    /**
     * ⑤ 用户鼠标 focus in，搜索框未输入任何内容。
     *
     * @param fp   解析类
     * @param user websocket用户信息
     * @return SuggestionResponse
     */
    public static SuggestionResponse suggestionsNull(final FocusParser fp, JSONObject user, String search, int endPos) {
        SuggestionResponse response = new SuggestionResponse(search);
        SuggestionDatas datas = new SuggestionDatas();

        datas.beginPos = 0;
//        datas.phraseBeginPos = datas.beginPos;
        String language = user.getString("language");
        String historyDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_history);
        String systemDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_system);
        String columnDescription = LanguageUtils.getMsg(language, LanguageUtils.SuggestionUtils_suggestion_description_column);

        boolean isQuestion = Constant.CategoryType.QUESTION.equalsIgnoreCase(user.getString("category"));

        if (isQuestion) {
            JSONArray historyQuestions = user.getJSONArray("historyQuestions");

            List<List<String>> bnfList = checkDefault();

            int count = 0;
            for (Object history : historyQuestions) {
                if (count >= historySize) {
                    break;
                }
                String suggestion = ((HistoryQuestion) history).question;
                SuggestionSuggestion ss = new SuggestionSuggestion();
                ss.beginPos = 0;
                ss.endPos = endPos;
                ss.suggestion = suggestion;
                ss.suggestionType = Constant.SuggestionType.HISTORY;
                ss.description = historyDescription;
                datas.addSug(ss);
                count++;
            }

            for (List<String> bnfName : bnfList) {
                for (String ruleName : bnfName) {
                    BnfRule br = fp.getRule(ruleName);
                    String suggestion = bnfRuleSug(fp, br).trim();
                    if (suggestion.isEmpty())
                        continue;
                    SuggestionSuggestion ss = new SuggestionSuggestion();
                    ss.beginPos = 0;
                    ss.endPos = endPos;
                    ss.suggestion = suggestion;
                    ss.suggestionType = Constant.SuggestionType.PHRASE;
                    ss.description = systemDescription;
                    datas.addSug(ss);
                }
            }
        }
        List<Column> columns = SourcesUtils.colRandomSuggestions(user);
        for (Column column : columns) {
            SuggestionSuggestion ss = new SuggestionSuggestion();
            ss.beginPos = 0;
            ss.endPos = endPos;
            ss.suggestion = column.getColumnDisplayName();
            ss.suggestionType = Constant.SuggestionType.COLUMN;
            ss.description = String.format(columnDescription, column.getColumnDisplayName());
            datas.addSug(ss);
        }

        response.setDatas(datas);
        return response;
    }

    /**
     * 当光标在search中间的时候，给出suggestion
     *
     * @param originTokens 原始问题的分词结果
     * @return SuggestionResponse
     */
    private static SuggestionResponse middlePosition(final FocusParser fp, String search, JSONObject user, List<FocusToken> originTokens, int position) throws IllegalException, IOException, AmbiguitiesException {
        String subSearch = search.substring(0, position);
        if (Common.isEmpty(subSearch)) {
            return new SuggestionResponse(search);
        }
        String category = user.getString("category");
        String language = user.getString("language");
        JSONObject amb = user.getJSONObject("ambiguities");
        @SuppressWarnings("unchecked")
        List<SourceReceived> srs = (List<SourceReceived>) user.get("sources");
        boolean isQuestion = Constant.CategoryType.QUESTION.equalsIgnoreCase(category);
        List<FocusToken> tokens = fp.focusAnalyzer.test(subSearch, language);
        FocusInst focusInst;
        if (isQuestion) {
            focusInst = fp.parseQuestion(tokens, amb, language, srs, Base.getFormula(user));
        } else {
            focusInst = fp.parseFormula(tokens, amb, language, srs);
        }
        JSONObject filter = Base.selectSource(focusInst, new ArrayList<>(), tokens.size(), user);
        JSONArray sourceList = filter.getJSONArray("sourceList");
        JSONArray columnIdList = new JSONArray();
        if (sourceList != null && !sourceList.isEmpty()) {
            columnIdList = SourcesUtils.getColumnIdList(sourceList, srs);
        }
        if (!focusInst.isInstruction) {
            SuggestionResponse response = suggestionsNotCompleted(fp, search, focusInst, user, tokens, position, columnIdList);
            int endPos = position;
            for (int i = 0; i < originTokens.size(); i++) {
                FocusToken token = originTokens.get(i);
                if (token.getStart() <= position && token.getEnd() >= position) {
                    endPos = token.getEnd();
                    if (i + 1 < originTokens.size()) {
                        FocusToken tmp = originTokens.get(i + 1);
                        if (Constant.END_QUOTES.contains(tmp.getWord())) {
                            endPos = tmp.getEnd();
                        }
                    }
                    break;
                }
            }
            for (SuggestionSuggestion suggestion : response.getDatas().suggestions) {
                suggestion.endPos = endPos;
            }
            return response;
        } else {
            return suggestionsCompleted(fp, search, focusInst, user, tokens, position, columnIdList);
        }
    }

    // 根据bnf规则名字获取所有 TokenString 的第一个terminalToken (function 除外)
    private static List<TerminalToken> terminalToken(FocusParser parser, BnfRule br) {
        List<TerminalToken> terminalTokens = new ArrayList<>();
        if (br != null && !br.getLeftHandSide().getName().endsWith("-function>")) {
            for (TokenString ts : br.getAlternatives()) {
                Token token = ts.getFirst();
                if (token instanceof TerminalToken) {
                    if (!((TerminalToken) token).getType().equals(Constant.FNDType.TABLE)
                            && !token.getName().equals(FormulaAnalysis.LEFT_BRACKET)) {
                        terminalTokens.add((TerminalToken) token);
                    }
                } else {
                    BnfRule brNext = parser.getRule(token.getName());
                    if (brNext == null) {
                        continue;
                    }
                    terminalTokens.addAll(terminalToken(parser, brNext));
                }
            }
        }
        return terminalTokens;
    }

    // 根据bnf规则名字给出一条完整的提示
    private static String bnfRuleSug(FocusParser parser, BnfRule br) {
        return bnfRuleSug(parser, br, new JSONArray());
    }

    // 根据bnf规则名字给出一条完整的提示(过滤部分列)
    private static String bnfRuleSug(FocusParser parser, BnfRule br, JSONArray columnIdList) {
        String sug = "";
        if (br != null) {
            for (TokenString ts : br.getAlternatives()) {
                String tmp = sug;
                boolean nextAlt = false;
                for (Token token : ts) {
                    if (token instanceof TerminalToken) {
                        String type = ((TerminalToken) token).getType();
                        if (!type.equals(Constant.FNDType.TABLE)) {
                            if (Constant.FNDType.INTEGER.equals(type) || Constant.FNDType.DOUBLE.equals(type)) {
                                sug = sug + SourcesUtils.decimalSug(true) + " ";
                            } else if (Constant.FNDType.COLUMN.equals(type)) {
                                if (!columnIdList.isEmpty() && !columnIdList.contains(((TerminalToken) token).getColumn().getColumnId())) {
                                    nextAlt = true;
                                    break;
                                }
                                sug = sug + token.getName() + " ";
                            } else {
                                sug = sug + token.getName() + " ";
                            }
                        }
                    } else {
                        BnfRule brNext = parser.getRule(token.getName());
                        if (brNext == null) {
                            nextAlt = true;
                            break;
                        }
                        String sugNext = bnfRuleSug(parser, brNext, columnIdList);
                        if (sugNext.isEmpty()) {
                            nextAlt = true;
                            break;
                        }
                        sug = sug + sugNext;
                    }
                }
                if (nextAlt) {
                    sug = tmp;
                } else {
                    break;
                }
            }
        }
        return sug;
    }

    private static List<List<String>> checkDefault() {
        return checkDefault(null);
    }

    private static List<List<String>> checkDefault(FocusInst focusInst) {
        List<List<String>> bnfList = new ArrayList<>();
        bnfList.addAll(DEFAULT_BNF);
        if (focusInst != null) {
            for (FocusPhrase fp : focusInst.getFocusPhrases()) {
                List<String> defaultBnf = checkDefault(fp, DEFAULT_BNF_DEEP);
                if (defaultBnf != null) {
                    bnfList.remove(defaultBnf);
                }
            }
        }
        return bnfList;
    }

    /**
     * 检测当前解析结果中是否含有预设的规则，返回预设的规则列表
     *
     * @param fp   解析类
     * @param deep 往下查找的 bnf 层级
     * @return 可以提示的规则列表
     */
    private static List<String> checkDefault(FocusPhrase fp, int deep) {
        for (List<String> defaultBnf : DEFAULT_BNF) {
            if (defaultBnf.contains(fp.getInstName())) {
                return defaultBnf;
            }
        }
        if (deep > 0) {
            for (FocusNode fn : fp.getFocusNodes()) {
                if (fn.isHasChild()) {
                    int tmpDeep = deep - 1;
                    List<String> tmp = checkDefault(fn.getChildren(), tmpDeep);
                    if (tmp != null) {
                        return tmp;
                    }
                }
            }
        }
        return null;
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
}
