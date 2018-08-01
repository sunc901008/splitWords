package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.chineseInstruction.CInstructionBuild;
import focus.search.instruction.filterInst.FilterInstruction;
import focus.search.instruction.phraseInst.PhraseInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
public class InstructionBuild {
    private static final Logger logger = Logger.getLogger(InstructionBuild.class);

    public static JSONObject build(FocusInst focusInst, String question, JSONObject amb, List<Formula> formulas, String language, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        JSONObject data = new JSONObject();
        data.put("question", question);

        JSONArray instructions = new JSONArray();

        List<FocusPhrase> focusPhrases = focusInst.getFocusPhrases();

        int index;
        int loop = 1;
        for (int i = 0; i < focusPhrases.size(); i++) {
            FocusPhrase focusPhrase = focusPhrases.get(i);
            if (focusPhrase.isSuggestion()) {
                continue;
            }
            logger.info("Build instruction. Loop:" + loop++ + " focusPhrase:" + focusPhrase.toJSON() + " ambiguities:" + amb + " language:" + language);
            index = instructions.size() / 2 + 1;
            FocusPhrase prePhrase = null;
            if (i > 0) {
                prePhrase = focusPhrases.get(i - 1);
            }
            instructions.addAll(build(prePhrase, focusPhrase, index, amb, formulas, language, dateColumns));
        }

        String ins = instructions.toJSONString();
        data.put("instructions", ins);
        return data;
    }

    public static JSONArray build(FocusPhrase prePhrase, FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, String language, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        if (Constant.Language.CHINESE.equals(language)) {
            return CInstructionBuild.build(focusPhrase, index, amb, formulas, dateColumns);
        }
        logger.info("Question: Start building instructions.  Language: English. focusPhrase:" + focusPhrase.toJSON() + " .Language:" + language);
        switch (focusPhrase.getInstName()) {
            case "<filter>":
                return FilterInstruction.build(prePhrase, focusPhrase, index, amb, formulas, dateColumns);
            case "<phrase>":
                return PhraseInstruction.build(focusPhrase, index, amb, formulas, dateColumns);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, String language, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        return build(null, focusPhrase, index, amb, new ArrayList<>(), language, dateColumns);
    }

}
