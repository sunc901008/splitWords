package focus.search.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import focus.search.analyzer.focus.FocusToken;
import focus.search.bnf.BnfRule;
import focus.search.bnf.FocusParser;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.tokens.NonTerminalToken;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.Token;
import focus.search.bnf.tokens.TokenString;
import focus.search.controller.common.Base;
import focus.search.meta.Column;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusParserException;
import org.apache.log4j.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.*;

/**
 * user: sunc
 * data: 2018/1/26.
 */
public class Constant {
    private static final Logger logger = Logger.getLogger(Constant.class);

    public static final String SUGGESTION = "suggestion";
    public static final String INSTRUCTION = "instruction";

    public static final String MINUS = "-";

    public static final String REDIS_INTEGER_PREFIX = "focus_integer";
    public static final String REDIS_DOUBLE_PREFIX = "focus_double";
    public static final String REDIS_KEYWORD_PREFIX = "%s_focus_keyword_%s";
    public static final String REDIS_TABLE_PREFIX = "%s_focus_table";// language_focus_table
    public static final String REDIS_COLUMN_PREFIX = "%s_focus_column_%s";// 根据data type区分 language_focus_column_int
    private static final String tableName = "focus";
    public static final String REDIS_RULE_PREFIX = "%s_focus_rule_%s";
    public static SerializerFeature[] features = new SerializerFeature[]{SerializerFeature.WriteClassName};

    public static final List<String> START_QUOTES = Arrays.asList("\"", "“", "'", "‘");
    public static final List<String> END_QUOTES = Arrays.asList("\"", "”", "'", "’");

    public static Integer BiTimeout = 120;

    public static String biHost = "localhost";
    public static Integer biPort = 8005;
    public static String biBaseUrl = "/api/bi_srv";

    public static String webServerHost = "localhost";
    public static Integer webServerPort = 8003;
    public static String webServerBaseUrl = "/api/bi";

    public static String ucHost = "localhost";
    public static Integer ucPort = 8003;
    public static String ucBaseUrl = "/api/uc";


    public static String indexHost = "localhost";
    public static Integer indexPort = 8999;
    public static String indexBaseUrl = "/index";

    public static boolean passUc = false;
    public static boolean passIndex = false;

    public static boolean debugLog = true;
    public static boolean rebuildRedis = true;

    private static final Properties properties = new Properties();

    private static void initConfig() {
        InputStream inputStream = null;
        try {
            File file = new File("/srv/focus/conf/search/config.properties");
            if (!file.exists()) {
                ResourceLoader resolver = new DefaultResourceLoader();
                file = resolver.getResource("conf/config.properties").getFile();
            }
            inputStream = new BufferedInputStream(new FileInputStream(file));
            properties.load(inputStream);
            biHost = properties.getProperty("biHost", "localhost");
            biPort = Integer.parseInt(properties.getProperty("biPort", "8005"));
            biBaseUrl = properties.getProperty("biBaseUrl", "/api/bi_srv");

            webServerHost = properties.getProperty("webServerHost", "localhost");
            webServerPort = Integer.parseInt(properties.getProperty("webServerPort", "8003"));
            webServerBaseUrl = properties.getProperty("webServerBaseUrl", "/api/bi");

            ucHost = properties.getProperty("ucHost", "localhost");
            ucPort = Integer.parseInt(properties.getProperty("ucPort", "8003"));
            ucBaseUrl = properties.getProperty("ucBaseUrl", "/api/uc");

            indexHost = properties.getProperty("indexHost", "localhost");
            indexPort = Integer.parseInt(properties.getProperty("indexPort", "8999"));
            indexBaseUrl = properties.getProperty("indexBaseUrl", "/index");

            passUc = Boolean.parseBoolean(properties.getProperty("passUc", "false"));
            passIndex = Boolean.parseBoolean(properties.getProperty("passIndex", "false"));
            debugLog = Boolean.parseBoolean(properties.getProperty("debugLog", "false"));
            rebuildRedis = Boolean.parseBoolean(properties.getProperty("rebuildRedis", "false"));

            BiTimeout = Integer.parseInt(properties.getProperty("biTimeout", "120"));

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public static void insertRedis() {
        initConfig();
        if (!rebuildRedis) {
            return;
        }
        RedisUtils.clear();
        long start = Common.getNow().getTimeInMillis();
        final Set<String> initEnglishKeywordRedis = new HashSet<>();
        FocusParser englishParser = Base.englishParser.deepClone();
        all(initEnglishKeywordRedis, englishParser.getAllRules(), "<question>");
        for (String keyword : initEnglishKeywordRedis) {
            initKeyword(englishParser, keyword, Language.ENGLISH);
        }
        List<String> englishColNames = getInitSource(englishParser, Language.ENGLISH);
        initSource(englishParser, tableName, Language.ENGLISH);
        initSource(englishParser, englishColNames, Language.ENGLISH);
        final Set<String> initChineseKeywordRedis = new HashSet<>();
        FocusParser chineseParser = Base.chineseParser.deepClone();
        all(initChineseKeywordRedis, chineseParser.getAllRules(), "<question>");
        for (String keyword : initChineseKeywordRedis) {
            if (initEnglishKeywordRedis.contains(keyword)) {
                continue;
            }
            initKeyword(chineseParser, keyword, Language.CHINESE);
        }
        List<String> chineseColNames = getInitSource(chineseParser, Language.CHINESE);
        initSource(chineseParser, tableName, Language.CHINESE);
        initSource(chineseParser, chineseColNames, Language.ENGLISH);
        long end = Common.getNow().getTimeInMillis();
        logger.info("init focus phrase and focus rule success. cost:" + (end - start));
    }

    private static void all(Set<String> terminals, List<BnfRule> rules, String ruleName) {
        BnfRule startRule = findRule(rules, ruleName);
        if (startRule != null)
            for (TokenString t : startRule.getAlternatives()) {
                Token token = t.peekFirst();
                if (token instanceof TerminalToken) {
                    terminals.add(token.getName());
                } else {
                    all(terminals, rules, token.getName());
                }
            }
    }

    private static BnfRule findRule(List<BnfRule> rules, String ruleName) {
        for (BnfRule br : rules) {
            if (br.getLeftHandSide().getName().equals(ruleName)) {
                return br;
            }
        }
        return null;
    }

    private static List<String> getInitSource(FocusParser fp, String language) {
        List<String> colNames = new ArrayList<>();
        String colName;
        String type;

        for (Column col : columns(language)) {
            type = col.getDataType();
            colName = col.getColumnDisplayName();
            BnfRule br = new BnfRule();
            BnfRule br1 = new BnfRule();
            br.setLeftHandSide(new NonTerminalToken(String.format("<%s-column>", type)));
            br1.setLeftHandSide(new NonTerminalToken(String.format("<table-%s-column>", type)));
            TokenString alternative_to_add = new TokenString();

            alternative_to_add.add(new TerminalToken(colName, Constant.FNDType.COLUMN, col));
            br.addAlternative(alternative_to_add);
            fp.addRule(br);

            TokenString alternative_to_add1 = new TokenString();
            alternative_to_add1.add(new TerminalToken(tableName, Constant.FNDType.TABLE));
            alternative_to_add1.add(new TerminalToken(colName, Constant.FNDType.COLUMN, col));
            br1.addAlternative(alternative_to_add1);
            fp.addRule(br1);
            colNames.add(colName);
        }
        return colNames;
    }

    private static List<Column> columns(String language) {
        List<Column> columns = new ArrayList<>();
        List<String> types = Arrays.asList(DataType.INT, DataType.DOUBLE, DataType.TIMESTAMP, DataType.BOOLEAN, DataType.STRING);
        int id = 1;
        for (String type : types) {
            Column col = new Column();
            col.setColumnDisplayName(String.format(REDIS_COLUMN_PREFIX, language, type));
            col.setColumnId(id++);
            col.setDataType(type);
            columns.add(col);
        }
        return columns;
    }

    private static void initKeyword(FocusParser parser, String keyword, String language) {
        try {
            String key = String.format(REDIS_KEYWORD_PREFIX, language, keyword);
            String type = FNDType.KEYWORD;
            if ("<integer>".equals(keyword)) {
                keyword = "1";
                type = FNDType.INTEGER;
                key = REDIS_INTEGER_PREFIX;
            } else if ("<double>".equals(keyword)) {
                keyword = "1.0";
                type = FNDType.DOUBLE;
                key = REDIS_DOUBLE_PREFIX;
            } else {// set rule into redis
                List<BnfRule> rules = parser.parseRules(new FocusToken(keyword, "", 0, keyword.length()));
                RedisUtils.set(String.format(REDIS_RULE_PREFIX, language, keyword), JSONArray.toJSONString(rules, features));
            }
            FocusToken focusToken = new FocusToken(keyword, type, 0, keyword.length());

            List<FocusPhrase> focusPhrases = parser.focusPhrases(focusToken, null, language);
            JSONArray jsonArray = new JSONArray();
            focusPhrases.forEach(f -> jsonArray.add(f.toJSON()));

            RedisUtils.set(key, jsonArray.toJSONString());
        } catch (FocusParserException | AmbiguitiesException e) {
            logger.warn(Common.printStacktrace(e));
        }
    }

    private static void initSource(FocusParser parser, String tableName, String language) {
        try {
            FocusToken focusToken = new FocusToken(tableName, FNDType.TABLE, 0, tableName.length());

            List<FocusPhrase> focusPhrases = parser.focusPhrases(focusToken, null, language);
            JSONArray jsonArray = new JSONArray();
            focusPhrases.forEach(f -> jsonArray.add(f.toJSON()));

            RedisUtils.set(String.format(REDIS_TABLE_PREFIX, language), jsonArray.toJSONString());
        } catch (FocusParserException | AmbiguitiesException e) {
            logger.warn(Common.printStacktrace(e));
        }
    }

    private static void initSource(FocusParser parser, List<String> colNames, String language) {
        for (String colName : colNames) {
            try {
                FocusToken focusToken = new FocusToken(colName, FNDType.COLUMN, 0, colName.length());

                List<FocusPhrase> focusPhrases = parser.focusPhrases(focusToken, null, language);
                JSONArray jsonArray = new JSONArray();
                focusPhrases.forEach(f -> jsonArray.add(f.toJSON()));

                RedisUtils.set(colName, jsonArray.toJSONString());
            } catch (FocusParserException | AmbiguitiesException e) {
                logger.warn(Common.printStacktrace(e));
            }
        }
    }

    // response status
    public static final class Status {
        public static final String SUCCESS = "success";
        public static final String ERROR = "error";
    }

    // language
    public static final class Language {
        public static final String ENGLISH = "english";
        public static final String CHINESE = "chinese";
    }

    //  search event type
    public static final class Event {
        public static final String TEXT_CHANGE = "textChange";
        public static final String FOCUS_IN = "focusIn";
    }

    //  node type
    public static final class FNDType {
        public static final String TABLE = "table";
        public static final String COLUMN = "column";
        public static final String INTEGER = "integer";
        public static final String DOUBLE = "double";
        public static final String SYMBOL = "symbol";
        public static final String KEYWORD = "keyword";
        public static final String FORMULA = "formulaName";
        public static final String COLUMN_VALUE = "columnValue";
        public static final String DATE_VALUE = "dateValue";
    }

    // category type
    public static final class CategoryType {
        public static final String QUESTION = "question";
        public static final String EXPRESSION = "expressionOrLogicalExpression";
    }

    // column type
    public static final class ColumnType {
        public static final String MEASURE = "measure";
        public static final String ATTRIBUTE = "attribute";
    }

    // data type
    public static final class DataType {
        public static final String STRING = "string";
        public static final String INT = "int";
        public static final String DOUBLE = "double";
        public static final String TIMESTAMP = "timestamp";
        public static final String BIGINT = "bigint";
        public static final String SMALLINT = "smallint";
        public static final String BOOLEAN = "boolean";
    }

    // build instruction type
    public static final class InstType {
        public static final String TABLE_COLUMN = "tblColumn";
        public static final String COLUMN = "column";
        public static final String STRING = "string";
        public static final String LIST = "list";
        public static final String FUNCTION = "function";
        public static final String NUMBER_FUNCTION = "numberFunction";
        public static final String DATE = "date";
        public static final String NUMBER = "number";
        public static final String FORMULA = "formula";
        public static final String COLUMN_VALUE = "columnValue";
    }

    // annotation type
    public static final class AnnotationType {
        public static final String PHRASE = "phrase";
        public static final String FILTER = "filter";
    }

    // annotation category
    public static final class AnnotationCategory {
        public static final String TOP_N = "topN";
        public static final String BOTTOM_N = "bottomN";
        public static final String SORT_BY_ORDER = "sortByOrder";
        public static final String GROWTH_OF_BY = "growthOfBy";
        public static final String FILTER = "filter";
        public static final String EXPRESSION = "expression";
        public static final String EXPRESSION_OR_LOGICAL = "expressionOrLogicalExpression";
        public static final String ATTRIBUTE_COLUMN = "attributeColumn";
        public static final String MEASURE_COLUMN = "measureColumn";
        public static final String FORMULA_NAME = "formulaName";
    }

    // annotation tokens type
    public static final class AnnotationTokenType {
        public static final String FILTER = "filter";
        public static final String PUNCTUATION_MARK = "punctuationMark";
        public static final String NUMBER = "number";
        public static final String SYMBOL = "symbol";
        public static final String STRING = "string";
    }

    // search engine error type
    public static final class ErrorType {
        public static final String ERROR = "error"; //请求出错
        public static final String NOT_LOGIN = "notLogin"; //您当前已退出登录，请重新登录
        public static final String BI_TIMEOUT = "biTimeout"; //BI返回数据超时
        public static final String NULL_SOURCETOKEN = "nullSourceToken"; //当前SourceToken为空
        public static final String NO_AMBIGUITY = "noAmbiguity"; //消除歧义时，请确保完全没有歧义
        public static final String AMBIGUITY_EXPIRED = "ambiguityExpired"; //消除歧义时，传入的歧义ID无法找到
        public static final String AMBIGUITY_OUT_OF_INDEX = "ambiguityIndexExceedLimit"; //消除歧义时，歧义的索引值超过上限
    }

    // search type; pinboard or search
    public static final class SearchOrPinboard {
        public static final String SEARCH_USER = "searchUser";
        public static final String PINBOARD_USER = "pinboardUser";
    }

    // aggregation type
    public static final class AggregationType {
        public static final String SUM = "SUM";
        public static final String MIN = "MIN";
        public static final String MAX = "MAX";
        public static final String AVERAGE = "AVERAGE";
        public static final String STD_DEVIATION = "STD_DEVIATION";
        public static final String VARIANCE = "VARIANCE";
        public static final String NONE = "NONE";
        public static final String COUNT = "COUNT";
        public static final String COUNT_DISTINCT = "COUNT_DISTINCT";
    }

    // ambiguity type
    public static final class AmbiguityType {
        // 列名出现歧义
        public static final String COLUMN = "column";
        // 中文分词出现歧义
        public static final String CHINESE = "chinese";
        // 记录歧义 before 、 after
        public static final Integer BEFORE = -1;
        public static final Integer AFTER = -2;
        // 记录歧义 last day/week/month/quarter/year
        public static final Integer LAST = -3;
        // 记录歧义 yearly/quarterly/monthly/weekly/daily
        public static final Integer DATE_INTERVAL = -4;
        // 记录歧义 between and
        public static final Integer BETWEEN_AND = -5;
        // 记录歧义 ago
        public static final Integer AGO = -6;

        public static final List<String> types = Arrays.asList("ago", "between_and", "date_interval", "last", "after", "before");

        public static String getWord(Integer type) {
            return types.get(types.size() + type);
        }

    }

    // symbol 中文|其他关键词 对应的符号
    public static final class SymbolMapper {
        public static final Map<String, String> symbol = new HashMap<>();

        static {
            symbol.put("大于", ">");
            symbol.put("小于", "<");
            symbol.put("不小于", ">=");
            symbol.put("大于等于", ">=");
            symbol.put("不大于", "<=");
            symbol.put("小于等于", "<=");
            symbol.put("等于", "=");
            symbol.put("不等于", "!=");
            symbol.put("before", "<");
            symbol.put("after", ">");
        }

    }

    // instId type
    public static final class InstIdType {
        public static final String ADD_LOGICAL_FILTER = "add_logical_filter";
        public static final String ADD_EXPRESSION = "add_expression";
        public static final String GROWTH_COLUMN = "add_column_for_growth";
        public static final String GROWTH_COLUMN_MEASURE = "add_column_measure_for_growth";
        public static final String GROWTH_DIMENSION = "use_column_for_growth_dimension";
        public static final String SORT_BY = "add_expression_for_sort";
        public static final String TOP_BOTTOM = "set_top_bottom_n";
        public static final String DATETIME_INTERVAL = "set_datetime_interval";
        public static final String SET_BI_CONFIG = "set_bi_config";

        public static final String ANNOTATION = "annotation";
    }

    // suggestion type
    public static final class SuggestionType {
        public static final String HISTORY = "history";
        public static final String PHRASE = "phrase";
        public static final String COLUMN = "column";
        public static final String NUMBER = "number";
        public static final String COLUMN_VALUE = "columnValue";
        public static final String DATE_VALUE = "dateValue";

    }

}