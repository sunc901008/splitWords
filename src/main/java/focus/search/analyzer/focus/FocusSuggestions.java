package focus.search.analyzer.focus;

/**
 * creator: sunc
 * date: 2018/1/18
 * description:
 */
public class FocusSuggestions {

    private String word;
    private String type;

    public FocusSuggestions() {

    }

    public FocusSuggestions(String word, String type) {
        this.word = word;
        this.type = type;
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
}
