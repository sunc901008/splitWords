package focus.search.instruction.chineseInstruction.chinesephraseInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.functionInst.OtherFuncInstruction;
import focus.search.instruction.sourceInst.AllColumnsInstruction;
import focus.search.meta.Column;
import focus.search.meta.Formula;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/17
 * description:
 */
//<phrase> := <top-n> |
//        <bottom-n> |
//        <sort-by> |
//        <growth-of> |
//        <all-columns> |
//        <number-complex-phrase> |
//        <other-function-columns>;
public class CPhraseInstruction {

    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas, List<Column> dateColumns) throws FocusInstructionException, IllegalException, AmbiguitiesException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<top-n>":
            case "<bottom-n>":
                return CTopBottomInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<sort-by>":
                return CSortByInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<growth-of>":
                return CGrowthOfInstruction.build(fn.getChildren(), index, amb);
            case "<all-columns>":
                return AllColumnsInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<number-complex-phrase>":
                return CPhraseNumberComplexInstruction.build(fn.getChildren(), index, amb, formulas);
//            case "<formula-column>":
//                return FormulaColumnInstruction.arg(fn.getChildren(), index, formulas);
            case "<other-function-columns>":
                return OtherFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

}
