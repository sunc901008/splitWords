package focus.search.response.search;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.instruction.annotations.AnnotationDatas;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class AnnotationResponse {
    public String question;
    public List<AnnotationDatas> datas = new ArrayList<>();

    public AnnotationResponse(String question) {
        this.question = question;
    }

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", "annotations");
        json.put("question", this.question);
        JSONArray jsonArray = new JSONArray();
        this.datas.forEach(data -> jsonArray.add(data.toJSON()));
        json.put("datas", jsonArray);
        return json.toJSONString();
    }
}
