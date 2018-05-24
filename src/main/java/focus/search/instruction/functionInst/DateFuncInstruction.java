package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.DateFunc.IfNullDateColFuncInstruction;
import focus.search.instruction.functionInst.DateFunc.IfThenElseDateColFuncInstruction;
import focus.search.instruction.functionInst.DateFunc.ToDateFuncInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/19
 * description:
 */

//<date-function-column> := <to_date-function> |
//        <if-then-else-date-column-function> |
//        <ifnull-date-column-function>;
public class DateFuncInstruction {

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_date-function>":
                return ToDateFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<if-then-else-date-column-function>":
                return IfThenElseDateColFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<ifnull-date-column-function>":
                return IfNullDateColFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_date-function>":
                return ToDateFuncInstruction.arg(fn.getChildren(), formulas);
            case "<if-then-else-date-column-function>":
                return IfThenElseDateColFuncInstruction.arg(fn.getChildren(), formulas);
            case "<ifnull-date-column-function>":
                return IfNullDateColFuncInstruction.arg(fn.getChildren(), formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation token
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<to_date-function>":
                return ToDateFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<if-then-else-date-column-function>":
                return IfThenElseDateColFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<ifnull-date-column-function>":
                return IfNullDateColFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }
}
