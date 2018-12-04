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

public class PreviewDataSetRequest {

    DataSource dataSource;
    /**
     * Internal key identifying this dataset
     */
    private String displayKey;
    /**
     * The item being previewed
     * this could be a path, or a table name
     */
    String previewItem;
    /**
     * the path of the item
     */
    String previewPath;
    /**
     * Additional propeties that will be added to the spark call
     */
    private Map<String,String> properties;
    /**
     * A schema parser to be used.  this is optional
     */
    private SchemaParserDescriptor schemaParser;

    private boolean addPreviewItemToPath;
    /**
     * A page spec for limiting
     *
     */
    private PageSpec pageSpec;

    /**
     * Should the system attempt to preview as plain text if it errors out with a different parser
     */
    private boolean fallbackToTextOnError;

    /**
     * boolean if its a file preview
     */
    private boolean isFilePreview;

    public PreviewDataSetRequest() {
    }

    public String getDisplayKey() {
        return displayKey;
    }

    public void setDisplayKey(String displayKey) {
        this.displayKey = displayKey;
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

    public boolean isAddPreviewItemToPath() {
        return addPreviewItemToPath;
    }

    public void setAddPreviewItemToPath(boolean addPreviewItemToPath) {
        this.addPreviewItemToPath = addPreviewItemToPath;
    }

    public boolean isFallbackToTextOnError() {
        return fallbackToTextOnError;
    }

    public void setFallbackToTextOnError(boolean fallbackToTextOnError) {
        this.fallbackToTextOnError = fallbackToTextOnError;
    }

    public boolean isFilePreview() {
        return isFilePreview;
    }

    public void setFilePreview(boolean filePreview) {
        isFilePreview = filePreview;
    }
}
