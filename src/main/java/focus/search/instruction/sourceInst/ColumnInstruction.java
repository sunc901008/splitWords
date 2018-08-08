package focus.search.instruction.sourceInst;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.meta.Formula;
import focus.search.response.exception.FocusInstructionException;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/18
 * description:
 */
//<all-*-column> := <*-column> |
//        <table-*-column> |
//        <*-formula-column>;
public class ColumnInstruction {
    private static final Logger logger = Logger.getLogger(ColumnInstruction.class);

    // table column | column | formula
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws FocusInstructionException {
        logger.debug("ColumnInstruction arg. focusPhrase:" + focusPhrase.toJSON());
        JSONObject expression = new JSONObject();
        FocusNode first = focusPhrase.getFocusNodes().get(0);
        if (first.getValue().endsWith("-formula-column>")) {// formula
            return FormulaColumnInstruction.arg(first.getChildren(), formulas);
        } else {
            expression.put("type", Constant.FNDType.COLUMN);
            expression.put("value", focusPhrase.getLastNode().getColumn().getColumnId());
        }
        return expression;
    }

}
