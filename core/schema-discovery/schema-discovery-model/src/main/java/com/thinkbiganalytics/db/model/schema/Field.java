/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.db.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Types;
import java.util.List;
import java.util.Vector;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {

    private String name;

    private String description = "";

    private String dataType;

    private String precisionScale;

    private Boolean primaryKey = false;

    private Boolean nullable = true;

    /**
     * is this field an indicator of when the row was created?
     */
    private Boolean createdTracker = false;

    /**
     * is this field an indicator of when thie row was updated
     */
    private Boolean updatedTracker = false;

    public List<String> sampleValues = new Vector<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (this.description == null ? "" : description);
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = (primaryKey == null ? false : primaryKey);
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = (nullable == null ? true : nullable);
    }

    public List<String> getSampleValues() {
        return sampleValues;
    }

    public void setSampleValues(List<String> sampleValues) {
        this.sampleValues = sampleValues;
    }

    public static String sqlTypeToDataType(Integer type) {

        if (type != null) {
            switch (type) {
                case Types.BIGINT:
                    return "bigint";
                case Types.NUMERIC:
                case Types.DOUBLE:
                case Types.DECIMAL:
                    return "double";
                case Types.INTEGER:
                    return "int";
                case Types.FLOAT:
                    return "float";
                case Types.TINYINT:
                    return "tinyint";
                case Types.DATE:
                    return "date";
                case Types.TIMESTAMP:
                    return "timestamp";
                case Types.BOOLEAN:
                    return "boolean";
                case Types.BINARY:
                    return "binary";
                default:
                    return "string";
            }
        }
        return null;
    }

    /**
     * Returns the structure in the format: Name | DataType | Desc | Primary \ CreatedTracker | UpdatedTracker
     * @return
     */
    public String asFieldStructure(){
        return name+"|"+getDataTypeWithPrecisionAndScale()+"|"+description+"|"+BooleanUtils.toInteger(primaryKey)+"|"+BooleanUtils.toInteger(createdTracker)+"|"+BooleanUtils.toInteger(updatedTracker);
    }

    public String getDataTypeWithPrecisionAndScale(){
        return dataType + (StringUtils.isNotBlank(precisionScale) ? "("+precisionScale+")" : "");
    }


    public String getPrecisionScale() {
        return precisionScale;
    }

    public void setPrecisionScale(String precisionScale) {
        this.precisionScale = precisionScale;
    }


    public Boolean getCreatedTracker() {
        return createdTracker;
    }

    public void setCreatedTracker(Boolean createdTracker) {
        this.createdTracker = createdTracker;
    }

    public Boolean getUpdatedTracker() {
        return updatedTracker;
    }

    public void setUpdatedTracker(Boolean updatedTracker) {
        this.updatedTracker = updatedTracker;
    }
}