package focus.search;

import focus.search.base.LoggerHandler;
import focus.search.bnf.FocusParser;
import focus.search.bnf.exception.InvalidRuleException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class Home {

    public static void main(String[] args) throws IOException, InvalidRuleException {

        DefaultModel.defaultRules();
        String question = "id > 5 sort by views desc";
        FocusParser.parse(question);


//        StackTraceElement[] trace = new Throwable().getStackTrace();
//        StackTraceElement tmp = trace[1];
//        System.out.println(tmp.getClassName() + "." + tmp.getMethodName()
//                + "(" + tmp.getFileName() + ":" + tmp.getLineNumber() + ")");
//        FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/conf/log4j.properties");
//        LogManager.getLogManager().readConfiguration(fis);
//        fis.close();

//        LoggerHandler.info("just a test.");


    }

    private static int test() {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        StackTraceElement tmp = trace[1];
        System.out.println(tmp.getClassName());
        System.out.println(tmp.getMethodName());
        System.out.println(tmp.getClassName() + "." + tmp.getMethodName()
                + "(" + tmp.getFileName() + ":" + tmp.getLineNumber() + ")");
        return tmp.getLineNumber();
    }


}
