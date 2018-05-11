package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.filterInst.FilterInstruction;
import focus.search.instruction.phraseInst.PhraseInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
public class InstructionBuild {
    private static final Logger logger = Logger.getLogger(InstructionBuild.class);

    public static JSONObject build(FocusInst focusInst, String question, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        JSONObject data = new JSONObject();
        data.put("question", question);

        JSONArray instructions = new JSONArray();

        List<FocusPhrase> focusPhrases = focusInst.getFocusPhrases();

        int index;
        int loop = 1;
        for (FocusPhrase focusPhrase : focusPhrases) {
            logger.info("Build instruction. Loop:" + loop++ + " focusPhrase:" + focusPhrase.toJSON() + " ambiguities:" + amb);
            index = instructions.size() / 2 + 1;
            instructions.addAll(build(focusPhrase, index, amb, formulas));
        }

        String ins = instructions.toJSONString();
        data.put("instructions", ins);
        return data;
    }

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        logger.info("Start building instructions. focusPhrase:" + focusPhrase.toJSON());
        switch (focusPhrase.getInstName()) {
            case "<filter>":
                return FilterInstruction.build(focusPhrase, index, amb, formulas);
            case "<phrase>":
                return PhraseInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
