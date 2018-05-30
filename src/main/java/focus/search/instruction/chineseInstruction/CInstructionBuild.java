package focus.search.instruction.chineseInstruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.chineseInstruction.chinesefilterInst.CFilterInstruction;
import focus.search.instruction.chineseInstruction.chinesephraseInst.CPhraseInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/5/22
 * description: 中文bnf指令
 */
public class CInstructionBuild {
    private static final Logger logger = Logger.getLogger(CInstructionBuild.class);

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        logger.info("Question: Start building instructions. Language: Chinese. focusPhrase:" + focusPhrase.toJSON());
        switch (focusPhrase.getInstName()) {
            case "<filter>":
                return CFilterInstruction.build(focusPhrase, index, amb, formulas, dateColumns);
            case "<phrase>":
                return CPhraseInstruction.build(focusPhrase, index, amb, formulas, dateColumns);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }


}
