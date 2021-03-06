package focus.search.analyzer.core;

public class CharacterUtil {

    public static final int CHAR_USELESS = 0;

    public static final int CHAR_ARABIC = 0X00000001;

    public static final int CHAR_ENGLISH = 0X00000002;

    public static final int CHAR_CHINESE = 0X00000004;

    public static final int CHAR_OTHER_CJK = 0X00000008;

    public static final int CHAR_PUNCTUATION = 0X00000003;

    // 单双引号
    public static final int QUOTE_CHAR = 0X00000005;

    // 逗号
    public static final int COMMA_CHAR = 0X00000006;

    /**
     * 识别字符类型
     *
     * @return int CharacterUtil定义的字符类型常量
     */
    public static int identifyCharType(char input) {
        if (input >= '0' && input <= '9' || PunctuationSegmenter.Num_Connector == input) {
            return CHAR_ARABIC;

        }
        if ((input >= 'a' && input <= 'z') || (input >= 'A' && input <= 'Z') || LetterSegmenter.CONNECT_SYMBOL.contains(input)) {
            return CHAR_ENGLISH;

        }
        if (PunctuationSegmenter.QUOTE_CHAR.contains(input)) {
            return QUOTE_CHAR;
        }
        if (PunctuationSegmenter.comma == input) {
            return COMMA_CHAR;
        }
        if (input == ' ') {// 空格不处理
            return CHAR_USELESS;

        }

        Character.UnicodeBlock ub = Character.UnicodeBlock.of(input);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
            // 目前已知的中文字符UTF-8集合
            return CHAR_CHINESE;

        }
        if (ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS // 全角数字字符和日韩字符
                // 韩文字符集
                || ub == Character.UnicodeBlock.HANGUL_SYLLABLES
                || ub == Character.UnicodeBlock.HANGUL_JAMO
                || ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                // 日文字符集
                || ub == Character.UnicodeBlock.HIRAGANA // 平假名
                || ub == Character.UnicodeBlock.KATAKANA // 片假名
                || ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS) {
            return CHAR_OTHER_CJK;

        }

        // 其他的不做处理的字符
        return CHAR_PUNCTUATION;
    }

    /**
     * 进行字符规格化（全角转半角，大写转小写处理）
     *
     * @return char
     */
    static char regularize(char input) {
        if (input == 12288) {
            input = (char) 32;

        } else if (input > 65280 && input < 65375) {
            input = (char) (input - 65248);

        } else if (input >= 'A' && input <= 'Z') {
            input += 32;
        }

        return input;
    }
}
