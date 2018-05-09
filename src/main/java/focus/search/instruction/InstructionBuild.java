package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.filterInst.FilterInstruction;
import focus.search.instruction.phraseInst.PhraseInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
public class InstructionBuild {

    public static JSONObject build(FocusInst focusInst, String question, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
        JSONObject data = new JSONObject();
        data.put("question", question);

        JSONArray instructions = new JSONArray();

        List<FocusPhrase> focusPhrases = focusInst.getFocusPhrases();

        int index;
        for (FocusPhrase focusPhrase : focusPhrases) {
            index = instructions.size() / 2 + 1;
            instructions.addAll(build(focusPhrase, index, amb, formulas));
        }

        String ins = instructions.toJSONString();
        data.put("instructions", ins);
        return data;
    }

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException {
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
