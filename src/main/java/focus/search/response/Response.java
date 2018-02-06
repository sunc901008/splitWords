package focus.search.response;

import com.alibaba.fastjson.JSONObject;

/**
 * creator: sunc
 * date: 2018/2/1
 * description:
 */
public class Response {
    private String type;
    private String command;
    private String sourceToken;
    private JSONObject datas;

    public Response() {

    }

    public Response(String type, String command) {
        this.type = type;
        this.command = command;
    }

    public Response(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public void setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
    }

    public JSONObject getDatas() {
        return datas;
    }

    public void setDatas(JSONObject datas) {
        this.datas = datas;
    }

    public String toString() {
        JSONObject json = new JSONObject();
        json.put("type", this.type);
        json.put("command", this.command);
        json.put("sourceToken", this.sourceToken);
        json.put("datas", this.datas);
        return json.toJSONString();
    }

}
