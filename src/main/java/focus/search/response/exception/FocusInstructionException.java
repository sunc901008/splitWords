package focus.search.response.exception;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class FocusInstructionException extends Exception {

    public FocusInstructionException(JSONObject msg) {
        super(msg.toJSONString());
    }

}
