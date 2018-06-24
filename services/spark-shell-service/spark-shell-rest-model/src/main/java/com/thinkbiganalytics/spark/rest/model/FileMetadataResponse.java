package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
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

import com.thinkbiganalytics.discovery.model.SchemaParserDescriptor;
import com.thinkbiganalytics.discovery.parser.SampleFileSparkScript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileMetadataResponse {

    private String message;
    private List<ParsedFileMetadata> fileMetadata;
    private Map<String, FileDataSet> datasets;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ParsedFileMetadata> getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(List<ParsedFileMetadata> fileMetadata) {
        this.fileMetadata = fileMetadata;
    }


    public Map<String, FileDataSet> getDatasets() {
        return datasets;
    }

    public void setDatasets(Map<String, FileDataSet> datasets) {
        this.datasets = datasets;
    }

    public static class FileDataSet {

        private List<ParsedFileMetadata> files;
        private SchemaParserDescriptor schemaParser;
        private SampleFileSparkScript sparkScript;
        private String mimeType;
        private String message;

        public List<ParsedFileMetadata> getFiles() {
            return files;
        }

        public void setFiles(List<ParsedFileMetadata> files) {
            this.files = files;
        }

        public SchemaParserDescriptor getSchemaParser() {
            return schemaParser;
        }

        public void setSchemaParser(SchemaParserDescriptor schemaParser) {
            this.schemaParser = schemaParser;
        }

        public SampleFileSparkScript getSparkScript() {
            return sparkScript;
        }

        public void setSparkScript(SampleFileSparkScript sparkScript) {
            this.sparkScript = sparkScript;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ParsedFileMetadata {

        String mimeType;
        String encoding;
        String delimiter;
        String filePath;
        String rowTag;
        String header;
        Map<String, String> properties;


        private transient List<Object> transformResultRow;

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public String getDelimiter() {
            return delimiter;
        }

        public void setDelimiter(String delimiter) {
            this.delimiter = delimiter;
        }

        public Integer getHeaderCount() {
            String headerCount = getProperties().get("headerCount");
            if (headerCount != null) {
                return new Integer(headerCount);
            } else {
                return 0;
            }
        }

        public String getHeader() {
            String headerCount = getProperties().get("header");
            if (headerCount == null) {
                headerCount = "";
            }
            return headerCount;
        }


        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getKey() {
            return mimeType + "-" + delimiter + "-" + getHeaderCount() + "-" + getHeader() + "-" + rowTag;
        }

        public String getRowTag() {
            return rowTag;
        }

        public void setRowTag(String rowTag) {
            this.rowTag = rowTag;
        }

        public Map<String, String> getProperties() {
            if(properties == null){
                properties = new HashMap<>();
            }
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        public List<Object> getTransformResultRow() {
            return transformResultRow;
        }

        public void setTransformResultRow(List<Object> transformResultRow) {
            this.transformResultRow = transformResultRow;
        }
    }
}
