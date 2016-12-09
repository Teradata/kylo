/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;

import com.thinkbiganalytics.discovery.schema.DataTypeDescriptor;

/**
 * Information about the data type
 */
public class DefaultDataTypeDescriptor implements DataTypeDescriptor{

    boolean isDate;

    boolean isNumeric;

    boolean isComplex;

    @Override
    public Boolean isDate() {
        return isDate;
    }

    @Override
    public Boolean isNumeric() {
        return isNumeric;
    }

    @Override
    public Boolean isComplex() {
        return isComplex;
    }

    public void setDate(boolean date) {
        isDate = date;
    }

    public void setNumeric(boolean numeric) {
        isNumeric = numeric;
    }

    public void setComplex(boolean complex) {
        isComplex = complex;
    }
}
