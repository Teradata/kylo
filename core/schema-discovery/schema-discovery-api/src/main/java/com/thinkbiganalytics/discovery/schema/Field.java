/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.schema;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public interface Field {

    String getName();

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

    String getDataTypeWithPrecisionAndScale();

    String getPrecisionScale();

    Boolean getCreatedTracker();

    Boolean getUpdatedTracker();

    /**
     * Returns the structure in the format: Name | DataType | Desc | Primary \ CreatedTracker | UpdatedTracker
     */
    String asFieldStructure();

}


