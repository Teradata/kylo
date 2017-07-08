package com.thinkbiganalytics.search.rest.model;

/*-
 * #%L
 * kylo-search-rest-model
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
 * Metadata about a Hive table column
 */
public class HiveColumn {

    private String columnName;
    private String columnType;
    private String columnComment;

    public HiveColumn() {
    }

    /**
     * Construct metadata for a Hive table column
     *
     * @param columnName    name of column
     * @param columnType    data type of column
     * @param columnComment comment on column
     */
    public HiveColumn(String columnName, String columnType, String columnComment) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnComment = columnComment;
    }

    /**
     * Get column name
     *
     * @return name of column
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Set column name
     *
     * @param columnName name of column
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Get column type
     *
     * @return data type of column
     */
    public String getColumnType() {
        return columnType;
    }

    /**
     * Set column type
     *
     * @param columnType data type of column
     */
    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    /**
     * Get column comment
     *
     * @return comment on column
     */
    public String getColumnComment() {
        return columnComment;
    }

    /**
     * Set column comment
     *
     * @param columnComment comment on column
     */
    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }
}
