package focus.search.instruction.functionInst;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.instruction.annotations.AnnotationToken;
import focus.search.instruction.functionInst.otherFunc.IfNullFuncInstruction;
import focus.search.instruction.functionInst.otherFunc.IfThenElseFuncInstruction;
import focus.search.instruction.nodeArgs.ColValueOrDateColInst;
import focus.search.instruction.nodeArgs.NumberArg;
import focus.search.instruction.sourceInst.ColumnInstruction;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import focus.search.response.exception.IllegalException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/26
 * description:
 */
//<other-function-columns> := <if-then-else-function> |
//                <ifnull-function>;
public class OtherFuncInstruction {
    private static final Logger logger = Logger.getLogger(OtherFuncInstruction.class);

    // 完整指令
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        logger.info("OtherFuncInstruction instruction build. focusPhrase:" + focusPhrase.toJSON());
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<if-then-else-function>":
                return IfThenElseFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            case "<ifnull-function>":
                return IfNullFuncInstruction.build(fn.getChildren(), index, amb, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<if-then-else-function>":
                return IfThenElseFuncInstruction.arg(fn.getChildren(), formulas);
            case "<ifnull-function>":
                return IfNullFuncInstruction.arg(focusPhrase, formulas);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    // annotation tokens
    public static List<AnnotationToken> tokens(FocusPhrase focusPhrase, List<Formula> formulas, JSONObject amb) throws FocusInstructionException {
        FocusNode fn = focusPhrase.getFocusNodes().get(0);
        switch (fn.getValue()) {
            case "<if-then-else-function>":
                return IfThenElseFuncInstruction.tokens(fn.getChildren(), formulas, amb);
            case "<ifnull-function>":
                return IfNullFuncInstruction.tokens(focusPhrase, formulas, amb);
            default:
                throw new FocusInstructionException(focusPhrase.toJSON());
        }
    }

    public static JSONObject arg(FocusNode param, List<Formula> formulas) throws FocusInstructionException, IllegalException {
        switch (param.getValue()) {
            case "<number-source-column>":
            case "<all-date-column>":
            case "<all-bool-column>":
            case "<all-string-column>":
                return ColumnInstruction.arg(param.getChildren());
            case "<single-column-value>":
                return ColumnValueInstruction.arg(param);
            case "<number>":
                return NumberArg.arg(param);
            case "<date-string-value>":
                return ColValueOrDateColInst.arg(param, formulas);
            default:
                throw new FocusInstructionException(param.toJSON());
        }
    }

    public static AnnotationToken token(FocusNode param, JSONObject amb) throws FocusInstructionException {
        switch (param.getValue()) {
            case "<number-source-column>":
            case "<all-date-column>":
            case "<all-bool-column>":
            case "<all-string-column>":
                return AnnotationToken.singleCol(param.getChildren(), amb);
            case "<single-column-value>":
                return ColumnValueInstruction.tokens(param).get(0);
            case "<number>":
                return NumberArg.token(param);
            case "<date-string-value>":
                AnnotationToken token = ColumnValueInstruction.tokens(param).get(0);
                token.type = Constant.InstType.DATE;
                return token;
            default:
                throw new FocusInstructionException(param.toJSON());
        }
    }

}
