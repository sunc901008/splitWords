package focus.search.instruction.annotations;

import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.meta.Formula;

/**
 * creator: sunc
 * date: 2018/4/25
 * description:
 */
public class AnnotationBuild {

    public static AnnotationDatas build(FocusPhrase focusPhrase, int index, JSONObject amb) {
        return build(focusPhrase, index, amb, null);
    }

    public static AnnotationDatas build(FocusPhrase focusPhrase, int index, JSONObject amb, Formula formula) {
        String instName = focusPhrase.getInstName();
        switch (instName) {
            default:
                return null;
        }
    }

}
