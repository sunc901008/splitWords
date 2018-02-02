package focus.search.base;

import org.apache.log4j.Logger;

/**
 * creator: sunc
 * date: 2018/2/1
 * description:
 */
public class LoggerHandler {
    private static final Logger logger = Logger.getLogger(LoggerHandler.class);

    public static void info(String msg, boolean printLog) {
        if (printLog) {
            classInfo();
            logger.info(msg);
        }
    }

    public static void info(String msg) {
        classInfo();
        logger.info(msg);
    }

    public static void error(String msg, boolean printLog) {
        if (printLog) {
            classInfo();
            logger.error(msg);
        }
    }

    public static void error(String msg) {
        classInfo();
        logger.error(msg);
    }

    public static void warn(String msg, boolean printLog) {
        if (printLog) {
            classInfo();
            logger.warn(msg);
        }
    }

    public static void warn(String msg) {
        classInfo();
        logger.warn(msg);
    }

    private static void classInfo() {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        StackTraceElement tmp = trace[2];
        String classInfo = tmp.getClassName() + "." + tmp.getMethodName() + "(" + tmp.getFileName() + ":" + tmp.getLineNumber() + ")";
        logger.info(classInfo);
    }

}
