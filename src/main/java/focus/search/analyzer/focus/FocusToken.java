package focus.search.analyzer.focus;

import com.alibaba.fastjson.JSON;

import java.util.HashSet;
import java.util.Set;

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
    private Set<FocusSuggestions> suggestions;
    private Set<String> ambiguities;

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

    public Set<FocusSuggestions> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(Set<FocusSuggestions> suggestions) {
        this.suggestions = suggestions;
    }

    public void addSuggestions(FocusSuggestions suggestion) {
        if (this.suggestions == null) {
            this.suggestions = new HashSet<>();
        }
        this.suggestions.add(suggestion);
    }

    public Set<String> getAmbiguities() {
        return ambiguities;
    }

    public void setAmbiguities(Set<String> ambiguities) {
        this.ambiguities = ambiguities;
    }

    public void addAmbiguities(String ambiguity) {
        if (this.ambiguities == null) {
            this.ambiguities = new HashSet<>();
        }
        this.ambiguities.add(ambiguity);
    }
}
