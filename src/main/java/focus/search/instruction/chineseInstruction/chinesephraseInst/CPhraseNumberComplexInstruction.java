package focus.search.instruction.chineseInstruction.chinesephraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/28
 * description:
 */
//<number-complex-phrase> := <average-phrase>;
public class CPhraseNumberComplexInstruction {
    private static final Logger logger = Logger.getLogger(CPhraseNumberComplexInstruction.class);

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        logger.info("CPhraseNumberComplexInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<average-phrase>":
                return CAverageInstruction.build(fn.getChildren(), index, amb, formulas);
            case "":
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }
}
