package focus.search.instruction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusInst;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/29
 * description:
 */
public class InstructionBuild {

    public static JSONObject build(FocusInst focusInst, String question, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONObject data = new JSONObject();
        data.put("question", question);

        JSONArray instructions = new JSONArray();

        List<FocusPhrase> focusPhrases = focusInst.getFocusPhrases();

        int index;
        for (FocusPhrase focusPhrase : focusPhrases) {
            index = instructions.size() / 2 + 1;
            instructions.addAll(build(focusPhrase.getInstName(), focusPhrase, index, amb, formulas));
        }

        data.put("instructions", instructions.toJSONString());
        return data;
    }

    private static JSONArray build(String instName, FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        switch (instName) {
            case "<simple-filter>":
                return SimpleInst.simpleFilter(focusPhrase, index, amb, formulas);
            case "<top-n>":
                return TopNInst.build(focusPhrase, index, amb, formulas);
            case "<all-column>":
                return SimpleInst.singleCol(focusPhrase, index, amb, formulas);
            case "<math-calculate>":
                return MathCalcInst.build(focusPhrase, index, amb);
            case "<bool-calculate>":
                return BoolCalcInst.build(focusPhrase, index, amb);
            default:
                throw new InvalidRuleException("Build instruction fail!!!");
        }
    }

}
