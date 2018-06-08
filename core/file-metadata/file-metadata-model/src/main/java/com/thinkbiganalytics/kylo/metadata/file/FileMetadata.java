package com.thinkbiganalytics.kylo.metadata.file;
/*-
 * #%L
 * kylo-file-metadata-model
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
import java.util.HashMap;
import java.util.Map;

public class FileMetadata {

    private String resource;

    private String mimeType;

    private String subType;

    private String encoding;

    private Map<String,String> properties;

    public FileMetadata(){
        this.properties = new HashMap<>();
    }

    public FileMetadata(String mimeType) {
        this();
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }


    public void addProperty(String key, String value){
        properties.put(key,value);
    }

    public void addProperties(Map<String,String> properties) {
        this.properties.putAll(properties);
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }
}
