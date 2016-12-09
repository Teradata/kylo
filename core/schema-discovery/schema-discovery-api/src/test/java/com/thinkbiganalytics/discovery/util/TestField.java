/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.util;

import com.thinkbiganalytics.discovery.schema.DataTypeDescriptor;
import com.thinkbiganalytics.discovery.schema.Field;

import java.util.List;
import java.util.Vector;

public class TestField implements Field {

    private String name;

    private String description = "";

    private String nativeDataType;

    private String derivedDataType;

    private Boolean primaryKey = false;

    private Boolean nullable = true;

    private Boolean modifiable = false;

    private DataTypeDescriptor dataTypeDescriptor;

    @Override
    public DataTypeDescriptor getDataTypeDescriptor() {
        return dataTypeDescriptor;
    }

    @Override
    public void setDataTypeDescriptor(DataTypeDescriptor dataTypeDescriptor) {
        this.dataTypeDescriptor = dataTypeDescriptor;
    }

    public List<String> sampleValues = new Vector<>();


    public void setNativeDataType(String nativeDataType) {
        this.nativeDataType = nativeDataType;
    }

    public String getDerivedDataType() {
        return derivedDataType;
    }


    @Override
    public Boolean isPrimaryKey() {
        return primaryKey;
    }

    @Override
    public Boolean isNullable() {
        return nullable;
    }

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

    @Override
    public void setDerivedDataType(String type) {
        this.derivedDataType = type;
    }

    public String asFieldStructure() {
        return name + "|" + derivedDataType;
    }

    @Override
    public String getNativeDataType() {
        return this.nativeDataType;
    }

    @Override
    public Boolean isModifiable() {
        return modifiable;
    }

    @Override
    public void setModifiable(Boolean isModifiable) {
        modifiable = isModifiable;
    }
}
