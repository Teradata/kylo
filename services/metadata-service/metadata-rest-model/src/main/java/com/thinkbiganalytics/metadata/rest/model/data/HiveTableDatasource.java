/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.data;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class HiveTableDatasource extends Datasource {

    private static final long serialVersionUID = -4852310850422331907L;

    private String database;
    private String tableName;
    private String modifiers;
    private List<HiveTableColumn> columns = new ArrayList<>();
    private List<HiveTablePartition> partitions = new ArrayList<>();

    public HiveTableDatasource() {
        super();
    }

    public HiveTableDatasource(String name, String database, String tableName) {
        super(name);
        this.database = database;
        this.tableName = tableName;
    }

    public String getDatabase() {
        return database;
    }


    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModifiers() {
        return modifiers;
    }

    public void setModifiers(String modifiers) {
        this.modifiers = modifiers;
    }

    public List<HiveTableColumn> getColumns() {
        return columns;
    }

    public void setFields(List<HiveTableColumn> fields) {
        this.columns = fields;
    }

    public List<HiveTablePartition> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<HiveTablePartition> partitions) {
        this.partitions = partitions;
    }
}
