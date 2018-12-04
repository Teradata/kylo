package com.thinkbiganalytics.discovery.model;

/*-
 * #%L
 * thinkbig-schema-discovery-model2
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

import com.thinkbiganalytics.discovery.util.TableSchemaType;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSet;

/**
 * Model describing a DataSet and schema paraser
 * this is used to call the respective Hive schema parser and return a Schema object with the proper serde
 */
public class DataSetSchemaRequest {

    private DataSet dataSet;

    private SchemaParserDescriptor schemaParser;

    private String tableSchemaType;

    public DataSetSchemaRequest(){

    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public SchemaParserDescriptor getSchemaParser() {
        return schemaParser;
    }

    public void setSchemaParser(SchemaParserDescriptor schemaParser) {
        this.schemaParser = schemaParser;
    }

    public String getTableSchemaType() {
        return tableSchemaType;
    }

    public void setTableSchemaType(String tableSchemaType) {
        this.tableSchemaType = tableSchemaType;
    }
}
