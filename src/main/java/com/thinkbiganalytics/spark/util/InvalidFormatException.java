/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.util;

import com.thinkbiganalytics.spark.HCatDataType;

/**
 * Represents a string type that cannot be converted to its numerical corresponding type
 */
public class InvalidFormatException extends Exception {

    private HCatDataType type;
    private String value;

    public InvalidFormatException(HCatDataType type, String value) {
        super("Value ["+value+"] cannot be converted to type "+type.getName());
        this.type = type;
        this.value = value;
    }

    public HCatDataType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
