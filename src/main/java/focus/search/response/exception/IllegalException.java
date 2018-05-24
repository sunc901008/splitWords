package focus.search.response.exception;

import focus.search.response.search.IllegalDatas;

/**
 * creator: sunc
 * date: 2018/5/23
 * description:
 */
public class IllegalException extends Exception {
    public IllegalDatas datas;
    public String question;

    public IllegalException(String msg) {
        super(msg);
    }

    public IllegalException(String msg, IllegalDatas datas) {
        super(msg);
        this.datas = datas;
    }

}
