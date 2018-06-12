package com.thinkbiganalytics.spark.rest.model;
/*-
 * #%L
 * Spark Shell Service REST Model
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
import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;

import java.util.Map;

public class DataSourceObjectPreviewRequest {

    DataSource dataSource;
    String previewItem;
    String previewPath;
    private Map<String,String> properties;
    private SchemaParserDescriptor schemaParser;
    private PageSpec pageSpec;

    public DataSourceObjectPreviewRequest() {
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getPreviewItem() {
        return previewItem;
    }

    public void setPreviewItem(String previewItem) {
        this.previewItem = previewItem;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public SchemaParserDescriptor getSchemaParser() {
        return schemaParser;
    }

    public void setSchemaParser(SchemaParserDescriptor schemaParser) {
        this.schemaParser = schemaParser;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public PageSpec getPageSpec() {
        return pageSpec;
    }

    public void setPageSpec(PageSpec pageSpec) {
        this.pageSpec = pageSpec;
    }
}
