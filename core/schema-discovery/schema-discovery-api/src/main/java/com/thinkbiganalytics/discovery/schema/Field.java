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
     *
     * @return name
     */
    String getName();

    /**
     * Business description of the field
     *
     * @return description
     */
    String getDescription();

    /**
     * The data type in reference of the source (e.g. an RDBMS)
     *
     * @return data type (native to database)
     */
    String getNativeDataType();

    /**
     * The data type in reference of the target (e.g. Hive)
     *
     * @return data type (in context of target system)
     */
    String getDerivedDataType();

    /**
     * Sets the data type in reference of the target (e.g. Hive)
     *
     * @param type data type (in context of target system)
     */
    void setDerivedDataType(String type);

    /**
     * Is this field the primary key
     *
     * @return true/false indicating if field is primary key
     */
    Boolean isPrimaryKey();

    /**
     * Can this field be set to null
     *
     * @return true/false indicating if field can accept null value
     */
    Boolean isNullable();

    /**
     * Get sample values for field
     *
     * @return list of sample values
     */
    List<String> getSampleValues();

    /**
     * Whether any derived properties of field can be modified.
     *
     * @return true/false indicating whether derived properties can be modified or not
     */
    Boolean isModifiable();

    /**
     * Sets whether derived properties of field can be modified.
     *
     * @param isModifiable true/false indicating if derived field properties can be modified or not
     */
    void setModifiable(Boolean isModifiable);

    /**
     * Additional descriptor about the derived data type
     *
     * @return {@link DataTypeDescriptor}
     */
    DataTypeDescriptor getDataTypeDescriptor();

    /**
     * Set additional description about the derived data type
     *
     * @param dataTypeDescriptor {@link DataTypeDescriptor}
     */
    void setDataTypeDescriptor(DataTypeDescriptor dataTypeDescriptor);

    /**
     * Returns the data type with the precision and scale
     *
     * @return data type with precision and scale
     */
    String getDataTypeWithPrecisionAndScale();

    /**
     * Returns the precision and scale portion of the data type
     *
     * @return precision and scale portion of the data type
     */
    String getPrecisionScale();

    /**
     * Whether field represents a record creation timestamp
     *
     * @return true/false indicating whether field represents created timestamp
     */
    Boolean getCreatedTracker();

    /**
     * Whether field represents a record update timestamp
     *
     * @return true/false indicating whether field represents update timestamp
     */
    Boolean getUpdatedTracker();

    /**
     * Returns the structure in a canonical format as follows: Name | DataType | Desc | Primary \ CreatedTracker | UpdatedTracker | otherName
     *@param otherName the name of the related column in the other table (either source or destination)
     * @return canonical format structure
     */
    String asFieldStructure(String otherName);

    /**
     * Gets tags assigned to this column.
     */
    List<Tag> getTags();
}


