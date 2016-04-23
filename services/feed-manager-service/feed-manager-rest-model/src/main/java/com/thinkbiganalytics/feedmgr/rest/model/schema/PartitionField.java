package com.thinkbiganalytics.feedmgr.rest.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Created by sr186054 on 1/27/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartitionField {

    public static enum PARTITON_FORMULA {
        YEAR("int"),MONTH("int"),DAY("int"),HOUR("int"),MIN("int"),SEC("int"),VAL("string",true);


        PARTITON_FORMULA(String defaultDataType) {
            this.formula = this.name();
            this.defaultDataType = defaultDataType;
            this.useColumnDataType = false;
        }
        PARTITON_FORMULA(String defaultDataType, boolean useColumnDataType) {
            this.formula = this.name().toLowerCase();
            this.defaultDataType =defaultDataType;
            this.useColumnDataType = useColumnDataType;
        }
        private boolean useColumnDataType;
        private String formula;
        private String defaultDataType;
        public String dataType(String columnDataType){
            if(useColumnDataType && columnDataType != null){
                return columnDataType;
            }
            else {
                return defaultDataType;
            }
        }

        public String getFormula() {
            return formula;
        }
    }

    private int position;
    private String sourceField;
    private String sourceDataType;
    private String field;
    private String formula;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public void setSourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }


    @JsonIgnore
    public String asPartitionStructure(){
        PARTITON_FORMULA f = PARTITON_FORMULA.valueOf(getFormula().toUpperCase());
        if(f != null){
           return this.field+"|"+f.dataType(this.sourceDataType);
        }
        return null;
    }

    @JsonIgnore
    public String asPartitionSpec(){
        PARTITON_FORMULA f = PARTITON_FORMULA.valueOf(getFormula().toUpperCase());
        if(f != null){
            if(f.equals(PARTITON_FORMULA.VAL)) {
                return this.field+"|"+f.dataType(this.sourceDataType)+"|"+this.sourceField.toLowerCase();
            }
            else {
                return this.field+"|"+f.dataType(this.sourceDataType)+"|"+f.name().toLowerCase()+"("+this.sourceField+")";
            }
        }

        return null;
    }
}
