/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.schema;

import java.util.List;

/**
 * Field of a schema object such as a table or file
 */
public interface Field {

    /**
     * Field or column name
     */
    String getName();

    /**
     * Business description of the field
     */
    String getDescription();

    /**
     * The data type in reference of the source (e.g. an RDBMS)
     */
    String getNativeDataType();

    /**
     * The data type in reference of the target (e.g. Hive)
     */
    String getDerivedDataType();

    /**
     * Sets the data type in reference of the target (e.g. Hive)
     */
    void setDerivedDataType(String type);

    Boolean isPrimaryKey();

    Boolean isNullable();

    List<String> getSampleValues();

    /**
     * Whether any derived properties of field can be modified.
     */
    Boolean isModifiable();

    /**
     * Sets whether derived properties of field can be modified.
     */
    void setModifiable(Boolean isModifiable);

    /**
     * Additional descriptor about the derived data type
     */
    DataTypeDescriptor getDataTypeDescriptor();

    /**
     * Additional description about the derived data type
     */
    void setDataTypeDescriptor(DataTypeDescriptor dataTypeDescriptor);

    /**
     * Returns the data type with the precision and scale
     */
    String getDataTypeWithPrecisionAndScale();

    /**
     * Returns the precision and scale portion of the data type
     */
    String getPrecisionScale();

    /**
     * Whether field represents a record creation timestamp
     */
    Boolean getCreatedTracker();

    /**
     * Whether field represents a record update timestamp
     */
    Boolean getUpdatedTracker();

    /**
     * Returns the structure in a canonical format as follows: Name | DataType | Desc | Primary \ CreatedTracker | UpdatedTracker
     */
    String asFieldStructure();

}


