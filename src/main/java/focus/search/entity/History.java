package focus.search.entity;

import org.apache.ibatis.type.Alias;

/**
 * creator: sunc
 * date: 2018/6/5
 * description:
 */
@Alias("history")
public class History {
    public int id;
    public String question;//search question
    public String sourceList;//source id list of the question(JSONArray string)
    public String language;//current language
    public int userId;//user info id
    public int score;//current question priority
    public String creationDate;
    public String updationDate;
}
