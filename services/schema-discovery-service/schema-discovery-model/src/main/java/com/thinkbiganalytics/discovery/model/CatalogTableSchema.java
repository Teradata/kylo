package com.thinkbiganalytics.discovery.model;

/*-
 * #%L
 * kylo-schema-discovery-model2
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSetTable;

import javax.annotation.Nonnull;

public class CatalogTableSchema extends DefaultTableSchema {

    private DataSetTable table;

    public CatalogTableSchema(@Nonnull final Schema schema) {
        setUuid(schema.getID());
        setName(schema.getName());
        setDescription(schema.getDescription());
        setCharset(schema.getCharset());
        setProperties(schema.getProperties());
        setFields(schema.getFields());

        if (schema instanceof TableSchema) {
            final TableSchema tableSchema = (TableSchema) schema;
            setSchemaName(tableSchema.getSchemaName());
            setDatabaseName(tableSchema.getDatabaseName());
        }
    }

    @JsonIgnore
    public DataSetTable getTable() {
        return table;
    }

    public void setTable(DataSetTable table) {
        this.table = table;
    }
}
