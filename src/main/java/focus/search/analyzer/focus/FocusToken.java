package focus.search.analyzer.focus;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/19
 * description:
 */
public class FocusToken {

    private String word;
    private String type;
    private int start;
    private int end;

    public FocusToken(String word, String type, int start, int end) {
        this.word = word;
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public static String tokensToString(List<FocusToken> tokens) {
        StringBuilder sb = new StringBuilder();
        FocusToken first = tokens.get(0);
        sb.append(first.getWord());
        int spaceStart = first.getEnd();
        int spaceCount;
        for (int i = 1; i < tokens.size(); i++) {
            FocusToken tmp = tokens.get(i);
            spaceCount = tmp.getStart() - spaceStart;
            spaceStart = tmp.getEnd();
            if (spaceCount > 0) {
                sb.append(" ");
            }
            sb.append(tmp.getWord());
        }
        return sb.toString();
    }
}
