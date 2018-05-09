package focus.search.base;

import com.alibaba.fastjson.serializer.NameFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Calendar;

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

    public static String decimalFormat(double d) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(d);
    }

    public static String cut(String msg) {
        return msg.substring(0, Math.min(1000, msg.length()));
    }

    public static String printStacktrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        try {
            sw.close();
        } catch (IOException ignored) {
        }
        pw.close();
        return sw.toString();
    }

    // 获取定时任务时间 cron
    public static String getCron() {
        String time = "%s %s %s * * *";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 120);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return String.format(time, hour, minute, second);
    }

}
