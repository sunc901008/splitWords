package focus.search.response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * creator: sunc
 * date: 2018/2/1
 * description:
 */
public interface JSONFormat {

    default JSONObject toJson() {
        return JSON.parseObject(JSON.toJSONString(this, SerializerFeature.WriteDateUseDateFormat));
    }

}