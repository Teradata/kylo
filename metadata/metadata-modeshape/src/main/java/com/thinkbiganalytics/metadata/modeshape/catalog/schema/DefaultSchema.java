package com.thinkbiganalytics.metadata.modeshape.catalog.schema;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.catalog.Schema;
import com.thinkbiganalytics.metadata.api.catalog.SchemaField;

import java.util.List;
import java.util.Map;

public class DefaultSchema implements Schema {

    private String charset;
    private List<? extends SchemaField> fields;
    private String description;
    private String title;
    private String systemName;

    @Override
    public String getCharset() {
        return this.charset;
    }

    @Override
    public void setCharset(String name) {
        this.charset = name;
    }

    @Override
    public List<? extends SchemaField> getFields() {
        return this.fields;
    }

    @Override
    public void setFields(List<? extends SchemaField> fields) {
        this.fields = fields;
    }

    @Override
    public <T> T getProperty(String name) {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public void setProperties(Map<String, Object> props) {

    }

    @Override
    public Map<String, Object> mergeProperties(Map<String, Object> props) {
        return null;
    }

    @Override
    public void setProperty(String key, Object value) {

    }

    @Override
    public void removeProperty(String key) {

    }

    @Override
    public String getSystemName() {
        return systemName;
    }

    @Override
    public void setSystemName(String name) {
        this.systemName = name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
