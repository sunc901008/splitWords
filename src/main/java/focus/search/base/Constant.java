package focus.search.base;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.Properties;

/**
 * user: sunc
 * data: 2018/1/26.
 */
public class Constant {

    public static final String SUGGESTION = "suggestion";
    public static final String INSTRUCTION = "instruction";

    public static Boolean PRINT_LOG = true;

    public static String biHost = "localhost";
    public static Integer biPort = 8005;
    public static String biBaseUrl = "/api/bi_srv";

    public static String webServerHost = "localhost";
    public static Integer webServerPort = 8004;
    public static String webServerBaseUrl = "/api/bi";

    public static String ucHost = "localhost";
    public static Integer ucPort = 8004;
    public static String ucBaseUrl = "/api/uc";

    public static boolean passUc = false;

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
            biHost = properties.getProperty("biHost");
            biPort = Integer.parseInt(properties.getProperty("biPort"));
            biBaseUrl = properties.getProperty("biBaseUrl");

            webServerHost = properties.getProperty("webServerHost");
            webServerPort = Integer.parseInt(properties.getProperty("webServerPort"));
            webServerBaseUrl = properties.getProperty("webServerBaseUrl");

            ucHost = properties.getProperty("ucHost", "localhost");
            ucPort = Integer.parseInt(properties.getProperty("ucPort"));
            ucBaseUrl = properties.getProperty("ucBaseUrl");

            passUc = Boolean.parseBoolean(properties.getProperty("passUc"));

            PRINT_LOG = Boolean.parseBoolean(properties.getProperty("printLog"));

        } catch (Exception e) {
            LoggerHandler.error(e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LoggerHandler.error(e.getMessage());
            }
        }
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
        public static final String COLUMNVALUE = "columnValue";

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

}
