package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.chineseInstruction.CInstructionBuild;
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
        return build(focusInst, question, amb, formulas, Constant.Language.ENGLISH);
    }

    public static JSONObject build(FocusInst focusInst, String question, JSONObject amb, List<Formula> formulas, String language) throws FocusInstructionException {
        JSONObject data = new JSONObject();
        data.put("question", question);

        JSONArray instructions = new JSONArray();

        List<FocusPhrase> focusPhrases = focusInst.getFocusPhrases();

        int index;
        int loop = 1;
        for (int i = 0; i < focusPhrases.size(); i++) {
            FocusPhrase focusPhrase = focusPhrases.get(i);
            logger.info("Build instruction. Loop:" + loop++ + " focusPhrase:" + focusPhrase.toJSON() + " ambiguities:" + amb);
            index = instructions.size() / 2 + 1;
            FocusPhrase prePhrase = null;
            if (i > 0) {
                prePhrase = focusPhrases.get(i - 1);
            }
            instructions.addAll(build(prePhrase, focusPhrase, index, amb, formulas, language));
        }

        String ins = instructions.toJSONString();
        data.put("instructions", ins);
        return data;
    }

    public static JSONArray build(FocusPhrase prePhrase, FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, String language) throws FocusInstructionException {
        if (Constant.Language.CHINESE.equals(language)) {
            return CInstructionBuild.build(focusPhrase, index, amb, formulas);
        }
        logger.info("Question: Start building instructions.  Language: English. focusPhrase:" + focusPhrase.toJSON() + " .Language:" + language);
        switch (focusPhrase.getInstName()) {
            case "<filter>":
                return FilterInstruction.build(prePhrase, focusPhrase, index, amb, formulas);
            case "<phrase>":
                return PhraseInstruction.build(focusPhrase, index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        return build(null, focusPhrase, index, amb, formulas, Constant.Language.ENGLISH);
    }

}
