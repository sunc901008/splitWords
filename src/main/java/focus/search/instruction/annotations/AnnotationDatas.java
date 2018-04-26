package focus.search.instruction.annotations;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/24
 * description:
 */
public class AnnotationDatas {
    public String type;
    public Integer id;
    public String category;
    public Integer begin;
    public Integer end;
    public JSONArray tokens;

    public AnnotationDatas() {
    }

    public AnnotationDatas(FocusPhrase focusPhrase, int index, String type, String category) {
        this.id = index;
        this.begin = focusPhrase.getFirstNode().getBegin();
        this.end = focusPhrase.getLastNode().getEnd();
        this.type = type;
        this.category = category;
    }

    public AnnotationDatas(FocusPhrase focusPhrase, int index, String type) {
        this.id = index;
        this.begin = focusPhrase.getFirstNode().getBegin();
        this.end = focusPhrase.getLastNode().getEnd();
        this.type = type;
    }

    public void addToken(AnnotationToken object) {
        if (this.tokens == null) {
            this.tokens = new JSONArray();
        }
        this.tokens.add(object);
    }

    public void addTokens(List<AnnotationToken> objects) {
        if (this.tokens == null) {
            this.tokens = new JSONArray();
        }
        this.tokens.addAll(objects);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", this.type);
        json.put("id", this.id);
        json.put("category", this.category);
        json.put("begin", this.begin);
        json.put("end", this.end);
        if (this.tokens != null && !this.tokens.isEmpty())
            json.put("tokens", this.tokens);
        return json;
    }
}
