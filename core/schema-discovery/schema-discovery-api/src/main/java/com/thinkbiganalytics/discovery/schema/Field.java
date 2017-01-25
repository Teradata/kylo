package com.thinkbiganalytics.discovery.schema;

/*-
 * #%L
 * thinkbig-schema-discovery-api
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


