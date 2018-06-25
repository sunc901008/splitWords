package focus.search.instruction.chineseInstruction.chinesephraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.phraseInst.numberComplexInst.CountInstruction;
import focus.search.instruction.phraseInst.numberComplexInst.VarianceInstruction;
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
//<number-complex-phrase> := <average-phrase> |
//                            <sum-phrase>
//                            <count-phrase>
//                            <max-min-phrase>
//                            <standard-deviation-phrase> |
//                            <unique-count-phrase> |
//                           <variance-phrase>;
public class CPhraseNumberComplexInstruction {
    private static final Logger logger = Logger.getLogger(CPhraseNumberComplexInstruction.class);

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        logger.info("CPhraseNumberComplexInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<average-phrase>":
                return CAverageInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<sum-phrase>":
                return CSumInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<count-phrase>":
                return CCountInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<max-min-phrase>":
                return CMaxMinInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<standard-deviation-phrase>":
                return CStandardDeviationInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<unique-count-phrase>":
                return CUniqueCountInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<variance-phrase>":
                return CVarianceInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }
}
