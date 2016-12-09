/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.schema;

/**
 * Provides additional information about the data type
 */
public interface DataTypeDescriptor {

    /**
     * Represents a date
     */
    Boolean isDate();

    /**
     * Represents a numeric type
     */
    Boolean isNumeric();

    /**
     * Whether the data type represents a complex type such as binary, structure, or array
     */
    Boolean isComplex();

}
