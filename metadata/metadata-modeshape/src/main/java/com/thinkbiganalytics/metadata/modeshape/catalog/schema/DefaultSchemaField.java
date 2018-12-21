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

import com.thinkbiganalytics.metadata.api.catalog.SchemaField;

import java.util.Set;
import java.util.TreeSet;

public class DefaultSchemaField implements SchemaField {

    private String name;
    private String systemName;
    private String description;
    private String datatype;
    private Set<String> tags = new TreeSet<>();

    @Override
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    @Override
    public String getDatatype() {
        return datatype;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getSystemName() {
        return systemName;
    }

    @Override
    public void setSystemName(String name) {
        systemName = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasTag(String tag) {
        return this.tags.contains(tag);
    }

    @Override
    public Set<String> getTags() {
        return this.tags;
    }

    @Override
    public Set<String> addTag(String tag) {
        this.tags.add(tag);
        return this.tags;
    }

    @Override
    public Set<String> removeTag(String tag) {
        this.tags.remove(tag);
        return tags;
    }

    @Override
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}
