package focus.search.base;

import com.alibaba.fastjson.serializer.NameFilter;
import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * creator: sunc
 * date: 2018/2/5
 * description:
 */
public class Common {
    private static final Logger logger = Logger.getLogger(Common.class);

    public static void send(WebSocketSession session, String message) throws IOException {
        send(session, new TextMessage(message));
    }

    private static void send(WebSocketSession session, TextMessage message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(message);
        } else {
            logger.warn("this session has closed.");
        }
    }

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

    public static boolean isNumber(String type) {
        return Constant.FNDType.INTEGER.equals(type) || Constant.FNDType.DOUBLE.equals(type);
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
        return 2000 > msg.length() ? msg : msg.substring(0, 2000) + "......";
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
        String time = "%s %s %s * * ?";
        Calendar calendar = getNow();
        logger.info("Current time : " + sdf1.format(calendar.getTime()));
        calendar.add(Calendar.SECOND, Constant.BiTimeout);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return String.format(time, second, minute, hour);
    }

    // 日期字符串格式化
    public static final SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH");
    private static final SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy/MM");
    private static final SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat sdf6 = new SimpleDateFormat("MM/dd");
    private static final List<SimpleDateFormat> sdfList = Arrays.asList(sdf0, sdf1, sdf2);

    private static String biTimeFormat(Date date) {
        return sdf0.format(date);
    }

    public static String biTimeFormat(Calendar date) {
        return biTimeFormat(date.getTime());
    }

    public static String dateFormat(String date) {
        date = date.trim();
        if (Common.isEmpty(date)) {
            logger.info(String.format("invalid input date: %s", date));
            return null;
        }
        String checkStr = date.substring(date.length() - 1);
        Pattern numberPattern = Pattern.compile("^[0-9]+$");
        if (!numberPattern.matcher(checkStr).find()) {
            logger.info(String.format("invalid input date: %s", date));
            return null;
        }
        Date d = null;
        if (!date.contains(" ")) {
            int length = date.length();
            if (length <= 10 && length >= 8) {
                d = dateParse(sdf3, date);
            } else if (length <= 7 && length >= 6) {
                d = dateParse(sdf4, date);
            } else if (length == 4 && !date.contains("/")) {
                d = dateParse(sdf5, date);
            } else if (length <= 5 && length >= 3) {
                d = dateParse(sdf6, date);
                if (d != null) {
                    Calendar calendar = getStartDay();
                    int year = calendar.get(Calendar.YEAR);
                    calendar.setTime(d);
                    calendar.set(Calendar.YEAR, year);
                    d = calendar.getTime();
                }
            }
            if (d != null) {
                return biTimeFormat(d);
            }
        } else {
            for (SimpleDateFormat sdf : sdfList) {
                d = dateParse(sdf, date);
                if (d != null) {
                    return biTimeFormat(d);
                }
            }
        }
        return null;
    }

    private static Date dateParse(SimpleDateFormat sdf, String date) {
        try {
            logger.info(String.format("current sdf pattern: %s . input date: %s", sdf.toPattern(), date));
            return sdf.parse(date);
        } catch (ParseException e) {
            logger.info("pattern fail.");
        }
        return null;
    }

    // 获取当前 起始时间 年月日时分秒
    public static Calendar getNow() {
        Calendar today = Calendar.getInstance();
        today.setFirstDayOfWeek(Calendar.MONDAY);
        return today;
    }

    // 获取当前 起始时间 年月日时00
    public static Calendar getStartHour() {
        Calendar today = getNow();
        clearMinute(today);
        return today;
    }

    // 获取当前 起始时间 年月日时分0
    public static Calendar getStartMinute() {
        Calendar today = getNow();
        clearSecond(today);
        return today;
    }

    // 获取当前天 起始时间 年月日000
    public static Calendar getStartDay() {
        Calendar today = getNow();
        clearTime(today);
        return today;
    }

    // 获取当前周 起始时间
    public static Calendar getStartWeek() {
        Calendar today = getNow();
        today.add(Calendar.DAY_OF_YEAR, today.getFirstDayOfWeek() - today.get(Calendar.DAY_OF_WEEK));
        clearTime(today);
        return today;
    }

    // 获取当前月 起始时间
    public static Calendar getStartMonth() {
        Calendar today = getNow();
        clearDay(today);
        clearTime(today);
        return today;
    }

    // 获取当前季度 起始时间
    public static Calendar getStartQuarter() {
        Calendar today = getNow();
        int currentMonth = today.get(Calendar.MONTH) + 1;
        try {
            if (currentMonth >= 1 && currentMonth <= 3)
                today.set(Calendar.MONTH, 0);
            else if (currentMonth >= 4 && currentMonth <= 6)
                today.set(Calendar.MONTH, 3);
            else if (currentMonth >= 7 && currentMonth <= 9)
                today.set(Calendar.MONTH, 6);
            else if (currentMonth >= 10 && currentMonth <= 12)
                today.set(Calendar.MONTH, 9);
        } catch (Exception e) {
            e.printStackTrace();
        }
        clearDay(today);
        clearTime(today);
        return today;
    }

    // 获取当前月 起始时间
    public static Calendar getStartYear() {
        Calendar today = getNow();
        clearMonth(today);
        clearDay(today);
        clearTime(today);
        return today;
    }

    // 将日期的天设置为1号
    private static void clearDay(Calendar today) {
        today.set(Calendar.DAY_OF_MONTH, 1);
    }

    // 将日期的月设置为1月
    private static void clearMonth(Calendar today) {
        today.set(Calendar.MONTH, 0);
    }

    // 将日期的时分秒设置为0
    private static void clearTime(Calendar today) {
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
    }

    // 将日期的分秒设置为0
    private static void clearMinute(Calendar today) {
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
    }

    // 将日期的秒设置为0
    private static void clearSecond(Calendar today) {
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
    }

    // 拷贝对象
    public static Object deepClone(Object t) {
        Object outer = null;
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(t);
            ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bai);
            outer = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return outer;
    }

    // debug 打印
    public static void info(Object object) {
        if (Constant.debugLog) {
            System.out.println(object);
        }
    }

}
