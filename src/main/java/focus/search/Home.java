package focus.search;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.exception.InvalidRuleException;

import java.io.IOException;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class Home {

    public static void main(String[] args) throws IOException, InvalidRuleException {

//        DefaultModel.defaultRules();
//        String question = "id > 5 sort by views desc";
//        FocusParser.parse(question);

        String cl = "{\"columnId\": 10,\"columnDisplayName\": \"age\",\"columnName\": \"age\",\"columnType\": \"doubleMeasure\",\"dataType\": " +
                "\"double\",\"description\": \"\",\"columnModify\": {\"updated\": \"2018-01-30 12:10:25\",\"name\": \"age\"}}";
        JSONObject json = JSONObject.parseObject(cl);
        for (String key : json.keySet()) {
            System.out.println("private String " + key + ";");
        }


    }

}
