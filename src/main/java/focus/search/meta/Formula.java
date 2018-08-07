package focus.search.meta;

import com.alibaba.fastjson.JSONObject;
import focus.search.base.Constant;
import focus.search.controller.common.FormulaAnalysis;

/**
 * creator: sunc
 * date: 2018/2/1
 * description:
 */
public class Formula {

    private String columnType;
    private String aggregation;
    private JSONObject instruction = new JSONObject();
    private String dataType;
    private String name;
    private String formula;
    private String id;

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getAggregation() {
        return aggregation;
    }

    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }

    public JSONObject getInstruction() {
        return instruction;
    }

    public void setInstruction(JSONObject instruction) {
        this.instruction = instruction;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("formula", formula);
        json.put("name", name);
        json.put("dataType", dataType);
        json.put("aggregation", aggregation);
        json.put("columnType", columnType);
        json.put("instruction", instruction);
        return json;
    }

    public String type() {
        if (FormulaAnalysis.BOOLEAN.equals(this.dataType)) {
            return Constant.DataType.BOOLEAN;
        } else if (FormulaAnalysis.TIMESTAMP.equals(this.dataType)) {
            return Constant.DataType.TIMESTAMP;
        } else if (FormulaAnalysis.STRING.equals(this.dataType)) {
            return Constant.DataType.STRING;
        }
        return Constant.DataType.DOUBLE;
    }

}
