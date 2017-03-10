package com.thinkbiganalytics.feedmgr.rest.model.schema;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartitionField {

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
    public String asPartitionStructure() {
        PARTITON_FORMULA f = PARTITON_FORMULA.valueOf(getFormula().toUpperCase());
        if (f != null) {
            return this.field + "|" + f.dataType(this.sourceDataType);
        }
        return null;
    }

    @JsonIgnore
    public String asPartitionSpec() {
        PARTITON_FORMULA f = PARTITON_FORMULA.valueOf(getFormula().toUpperCase());
        if (f != null) {
            if (f.equals(PARTITON_FORMULA.VAL)) {
                return this.field + "|" + f.dataType(this.sourceDataType) + "|" + this.sourceField.toLowerCase();
            } else {
                return this.field + "|" + f.dataType(this.sourceDataType) + "|" + f.name().toLowerCase() + "(" + this.sourceField + ")";
            }
        }

        return null;
    }

    public static enum PARTITON_FORMULA {
        TO_DATE("date"), YEAR("int"), MONTH("int"), DAY("int"), HOUR("int"), MIN("int"), SEC("int"), VAL("string", true);


        private boolean useColumnDataType;
        private String formula;
        private String defaultDataType;

        PARTITON_FORMULA(String defaultDataType) {
            this.formula = this.name();
            this.defaultDataType = defaultDataType;
            this.useColumnDataType = false;
        }

        PARTITON_FORMULA(String defaultDataType, boolean useColumnDataType) {
            this.formula = this.name().toLowerCase();
            this.defaultDataType = defaultDataType;
            this.useColumnDataType = useColumnDataType;
        }

        public String dataType(String columnDataType) {
            if (useColumnDataType && columnDataType != null) {
                return columnDataType;
            } else {
                return defaultDataType;
            }
        }

        public String getFormula() {
            return formula;
        }
    }
}
