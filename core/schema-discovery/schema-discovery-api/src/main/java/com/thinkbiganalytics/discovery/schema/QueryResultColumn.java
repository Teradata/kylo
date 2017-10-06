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

/**
 * Represent a column in the query result
 */
public interface QueryResultColumn {

    /**
     * Get column type
     *
     * @return data type
     */
    String getDataType();

    /**
     * Set column type
     *
     * @param dataType data type
     */
    void setDataType(String dataType);

    /**
     * Gets the database-specific type name.
     */
    String getNativeDataType();

    /**
     * Sets the database-specific type name.
     */
    void setNativeDataType(String nativeDataType);

    /**
     * Get table name
     *
     * @return table name
     */
    String getTableName();

    /**
     * Set table name
     *
     * @param tableName table name
     */
    void setTableName(String tableName);

    /**
     * Get database name
     *
     * @return database name
     */
    String getDatabaseName();


    /**
     * Set database name
     *
     * @param databaseName database name
     */
    void setDatabaseName(String databaseName);


    /**
     * Get column display name
     *
     * @return display name
     */
    String getDisplayName();

    /**
     * Set column display name
     *
     * @param displayName display name
     */
    void setDisplayName(String displayName);

    /**
     * Get column label in hive
     *
     * @return column label
     */
    String getHiveColumnLabel();

    /**
     * Set column label in hive
     *
     * @param hiveColumnLabel column label
     */
    void setHiveColumnLabel(String hiveColumnLabel);

    /**
     * Get column index
     *
     * @return index
     */
    int getIndex();

    /**
     * Set column index
     *
     * @param index index
     */
    void setIndex(int index);

    /**
     * Get column field
     *
     * @return field
     */
    String getField();

    /**
     * Set column field
     *
     * @param field field
     */
    void setField(String field);

    /**
     * Gets a human-readable description of this column.
     *
     * @return the comment
     */
    String getComment();

    /**
     * Sets a human-readable description for this column.
     *
     * @param comment the comment
     */
    void setComment(String comment);
}
