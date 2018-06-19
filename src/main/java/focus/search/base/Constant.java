package focus.search.base;

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
    public static Integer ucPort = 8004;
    public static String ucBaseUrl = "/api/uc";


    public static String indexHost = "localhost";
    public static Integer indexPort = 8999;
    public static String indexBaseUrl = "/index";

    public static boolean passUc = false;
    public static boolean passIndex = false;

    private static final Properties properties = new Properties();

    static {
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
            ucPort = Integer.parseInt(properties.getProperty("ucPort", "8004"));
            ucBaseUrl = properties.getProperty("ucBaseUrl", "/api/uc");

            indexHost = properties.getProperty("indexHost", "localhost");
            indexPort = Integer.parseInt(properties.getProperty("indexPort", "8999"));
            indexBaseUrl = properties.getProperty("indexBaseUrl", "/index");

            passUc = Boolean.parseBoolean(properties.getProperty("passUc", "false"));
            passIndex = Boolean.parseBoolean(properties.getProperty("passIndex", "false"));

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

        public static final List<String> types = Arrays.asList("between_and", "date_interval", "last", "after", "before");

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