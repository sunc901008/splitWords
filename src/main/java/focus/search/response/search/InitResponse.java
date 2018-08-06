package focus.search.response.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import focus.search.meta.Formula;
import focus.search.meta.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/1
 * description:
 */
public class InitResponse {
    private String type;
    private String command;
    private String sourceToken;
    private JSONObject datas;

    public InitResponse(String type, String command) {
        this.type = type;
        this.command = command;
    }

    public InitResponse(String type) {
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

    public String response() {
        JSONObject json = new JSONObject();
        json.put("type", this.type);
        json.put("command", this.command);
        json.put("sourceToken", this.sourceToken);
        json.put("datas", this.datas);
        return json.toJSONString();
    }

    public static class Init {
        private String status;
        private String message;
        private List<Source> sources = new ArrayList<>();
        private List<Formula> formulas = new ArrayList<>();

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<Source> getSources() {
            return sources;
        }

        public void setSources(List<Source> sources) {
            this.sources = sources;
        }

        public void addSource(Source source) {
            this.sources.add(source);
        }

        public List<Formula> getFormulas() {
            return formulas;
        }

        public void setFormulas(List<Formula> formulas) {
            this.formulas = formulas;
        }

        public JSONObject toJson() {
            return JSON.parseObject(JSON.toJSONString(this, SerializerFeature.WriteDateUseDateFormat));
        }
    }

}
