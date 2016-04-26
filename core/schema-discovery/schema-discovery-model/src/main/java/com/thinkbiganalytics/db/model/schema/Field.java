/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.db.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Types;
import java.util.List;
import java.util.Vector;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {

    private String name;

    private String description = "";

    private String dataType;

    private Boolean primaryKey = false;

    private Boolean nullable = true;

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

    public String asFieldStructure(){
        return name+"|"+dataType;
    }
}