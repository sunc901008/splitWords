package focus.search.instruction.functionInst.numberFunc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.bnf.FocusNode;
import focus.search.bnf.FocusPhrase;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.instruction.AnnotationBuild;
import focus.search.instruction.sourceInst.ColumnValueInstruction;
import focus.search.instruction.sourceInst.DateColInstruction;
import focus.search.meta.Formula;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/4/20
 * description:
 */
//<diff_days-function> := diff_days ( <column-value> , <date-columns> ) |
//        diff_days ( <date-columns> , <date-columns> ) |
//        diff_days ( <date-columns> , <column-value> ) |
//        diff_days ( <column-value> , <column-value> );
public class DiffDaysFuncInstruction {

    // 完整指令 diff_days
    public static JSONArray build(FocusPhrase focusPhrase, int index, JSONObject amb, List<Formula> formulas) throws InvalidRuleException {
        JSONArray instructions = new JSONArray();
        JSONArray annotationId = new JSONArray();
        annotationId.add(index);
        JSONObject json1 = new JSONObject();
        json1.put("annotationId", annotationId);
        json1.put("instId", "add_logical_filter");

        json1.put("expression", arg(focusPhrase, formulas));
        instructions.add(json1);

        JSONObject json2 = new JSONObject();
        json2.put("annotationId", annotationId);
        json2.put("instId", "annotation");

        // annotation content
        json2.put("content", AnnotationBuild.build(focusPhrase, index, amb));

        instructions.add(json2);

        return instructions;
    }

    // 其他指令一部分
    public static JSONObject arg(FocusPhrase focusPhrase, List<Formula> formulas) throws InvalidRuleException {
        FocusNode param1 = focusPhrase.getFocusNodes().get(2);
        FocusNode param2 = focusPhrase.getFocusNodes().get(4);

        JSONObject arg = new JSONObject();
        arg.put("type", Constant.InstType.FUNCTION);
        arg.put("name", focusPhrase.getNodeNew(0).getValue());
        JSONArray args = new JSONArray();

        if ("<column-value>".equals(param1.getValue())) {
            args.add(ColumnValueInstruction.arg(param1));
        } else {//<date-columns>
            args.add(DateColInstruction.arg(param1.getChildren(), formulas));
        }

        if ("<column-value>".equals(param2.getValue())) {
            args.add(ColumnValueInstruction.arg(param1));
        } else {//<date-columns>
            args.add(DateColInstruction.arg(param2.getChildren(), formulas));
        }

        arg.put("args", args);
        return arg;
    }

}
