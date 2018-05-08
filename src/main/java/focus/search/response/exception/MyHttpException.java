package focus.search.response.exception;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class MyHttpException extends Exception {
    public MyHttpException() {
        super("Http connect exception!");
    }
}
