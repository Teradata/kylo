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
 * Stores the search results coming from a table
 */
public class TableSearchResultData extends AbstractSearchResultData {

    private String schemaName;
    private String tableName;
    private List<Pair> columnNamesAndValues;

    public TableSearchResultData() {
        final String ICON = "toc";
        final String COLOR = "Navy";
        super.setIcon(ICON);
        super.setColor(COLOR);
        super.setType(SearchResultType.KYLO_DATA);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Pair> getColumnNamesAndValues() {
        return columnNamesAndValues;
    }

    public void setColumnNamesAndValues(List<Pair> columnNamesAndValues) {
        this.columnNamesAndValues = columnNamesAndValues;
    }
}
