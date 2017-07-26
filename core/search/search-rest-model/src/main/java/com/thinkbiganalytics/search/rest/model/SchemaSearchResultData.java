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

import java.util.List;

/**
 * Stores the search results coming from a schema
 */
public class SchemaSearchResultData extends AbstractSearchResultData {

    private String databaseName;
    private String databaseOwner;
    private String tableCreateTime;
    private String tableName;
    private String tableType;
    private List<HiveColumn> hiveColumns;

    public SchemaSearchResultData() {
        final String ICON = "grid_on";
        final String COLOR = "DarkGreen";
        super.setIcon(ICON);
        super.setColor(COLOR);
        super.setType(SearchResultType.KYLO_SCHEMA);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseOwner() {
        return databaseOwner;
    }

    public void setDatabaseOwner(String databaseOwner) {
        this.databaseOwner = databaseOwner;
    }

    public String getTableCreateTime() {
        return tableCreateTime;
    }

    public void setTableCreateTime(String tableCreateTime) {
        this.tableCreateTime = tableCreateTime;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public List<HiveColumn> getHiveColumns() {
        return hiveColumns;
    }

    public void setHiveColumns(List<HiveColumn> hiveColumns) {
        this.hiveColumns = hiveColumns;
    }
}
