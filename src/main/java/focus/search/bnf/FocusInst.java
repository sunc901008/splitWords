package focus.search.bnf;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusInst {

    private List<FocusPhrase> focusPhrases = new ArrayList<>();

    public List<FocusPhrase> getFocusPhrases() {
        return focusPhrases;
    }

    public void setFocusPhrases(List<FocusPhrase> focusPhrases) {
        this.focusPhrases = focusPhrases;
    }

    public void addPfs(FocusPhrase focusPhrase) {
        this.focusPhrases.add(focusPhrase);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        focusPhrases.forEach(f -> jsonArray.add(f.toJSON()));
        json.put("focusPhrases", jsonArray);
        return json;
    }

}
