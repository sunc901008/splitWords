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

    public boolean isInstruction = false;
    private List<FocusPhrase> focusPhrases = new ArrayList<>();

    // 记录出错位置
    public int position = -1;

    public List<FocusPhrase> getFocusPhrases() {
        return focusPhrases;
    }

    public FocusNode firstNode() {
        if (focusPhrases.isEmpty())
            return null;
        return focusPhrases.get(0).getFirstNode();
    }

    public FocusPhrase firstFocusPhrase() {
        if (focusPhrases.isEmpty())
            return null;
        return focusPhrases.get(0);
    }

    public void setFocusPhrases(List<FocusPhrase> focusPhrases) {
        this.focusPhrases = focusPhrases;
    }

    public void addPf(FocusPhrase focusPhrase) {
        this.focusPhrases.add(focusPhrase);
    }

    public void addPfs(List<FocusPhrase> focusPhrases) {
        this.focusPhrases.addAll(focusPhrases);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        focusPhrases.forEach(f -> jsonArray.add(f.toJSON()));
        json.put("focusPhrases", jsonArray);
        json.put("position", position);
        json.put("isInstruction", isInstruction);
        return json;
    }

    public int size() {
        return focusPhrases.size();
    }

}
