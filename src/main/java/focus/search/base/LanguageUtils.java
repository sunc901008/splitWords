package focus.search.base;

import org.apache.log4j.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.Properties;

/**
 * creator: sunc
 * date: 2018/7/4
 * description:
 */
public class LanguageUtils {
    private static final Logger logger = Logger.getLogger(LanguageUtils.class);
    private static final Properties englishProperties = new Properties();
    private static final Properties chineseProperties = new Properties();

    public static final String modelNameCheck_invalid_character = "modelNameCheck_invalid_character";
    public static final String modelNameCheck_keyword = "modelNameCheck_keyword";
    public static final String modelNameCheck_pure_digital = "modelNameCheck_pure_digital";
    public static final String IllegalDatas_reason = "IllegalDatas_reason";
    public static final String IllegalDatas_reason_item = "IllegalDatas_reason_item";
    public static final String SuggestionUtils_string_guidance = "SuggestionUtils_string_guidance";
    public static final String SuggestionUtils_date_guidance = "SuggestionUtils_date_guidance";
    public static final String SuggestionUtils_suggestion_description_column = "SuggestionUtils_suggestion_description_column";
    public static final String SuggestionUtils_suggestion_description_formula = "SuggestionUtils_suggestion_description_formula";
    public static final String SuggestionUtils_suggestion_description_history = "SuggestionUtils_suggestion_description_history";
    public static final String SuggestionUtils_suggestion_description_system = "SuggestionUtils_suggestion_description_system";
    public static final String SuggestionUtils_suggestion_description_number = "SuggestionUtils_suggestion_description_number";
    public static final String SuggestionUtils_suggestion_description_column_value = "SuggestionUtils_suggestion_description_column_value";
    public static final String SuggestionUtils_suggestion_description_date_value = "SuggestionUtils_suggestion_description_date_value";
    public static final String Ambiguity_title = "Ambiguity_title";
    public static final String Ambiguity_item = "Ambiguity_item";

    static {
        InputStream inputStream = null;
        try {
            ResourceLoader resolver = new DefaultResourceLoader();
            File file = resolver.getResource("info_en_US.properties").getFile();
            inputStream = new BufferedInputStream(new FileInputStream(file));
            englishProperties.load(inputStream);

            file = resolver.getResource("info_zh_CN.properties").getFile();
            inputStream = new BufferedInputStream(new FileInputStream(file));
            chineseProperties.load(inputStream);

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

    public static String getMsg(String language, String key) {
        if (Constant.Language.CHINESE.equals(language)) {
            return chineseProperties.getProperty(key);
        }
        return englishProperties.getProperty(key);
    }

}
