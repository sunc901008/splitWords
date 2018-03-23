package focus.search.base;

import com.alibaba.fastjson.serializer.NameFilter;

/**
 * creator: sunc
 * date: 2018/2/5
 * description:
 */
public class Common {

    private static String UnderLineString2Camel(String name) {
        String[] str = name.split("_");
        StringBuilder sb = new StringBuilder();
        sb.append(str[0]);
        for (int i = 1; i < str.length; i++) {
            String tmp = str[i];
            sb.append(tmp.substring(0, 1).toUpperCase());
            sb.append(tmp.substring(1));
        }
        return sb.toString();
    }

    public static class JSONFilter implements NameFilter {

        @Override
        public String process(Object object, String name, Object value) {
            return Common.UnderLineString2Camel(name);
        }
    }

    public static boolean intCheck(String s) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean doubleCheck(String s) {
        if (!s.contains(".")) {
            return false;
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isEmpty(Object object) {
        return object == null || object.toString().isEmpty();
    }

}
