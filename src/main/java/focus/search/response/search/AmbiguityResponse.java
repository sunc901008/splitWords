package focus.search.response.search;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/7
 * description:
 */
public class AmbiguityResponse {
    private String question;
    private List<Datas> datas = new ArrayList<>();

    public AmbiguityResponse(String question) {
        this.question = question;
    }

    public static class Datas {
        public String title;
        public String id;
        public String begin;
        public String end;
        public List<String> possibleMenus;
    }

}
