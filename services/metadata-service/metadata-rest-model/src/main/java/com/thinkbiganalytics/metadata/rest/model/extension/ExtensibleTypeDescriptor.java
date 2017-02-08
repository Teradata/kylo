/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.extension;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtensibleTypeDescriptor {

    private String id;
    private String name;
    private String supertype;
    private String displayName;
    private String description;
    private DateTime createdTime;
    private DateTime modifiedTime;
    private Set<FieldDescriptor> fields = new HashSet<>();

    public ExtensibleTypeDescriptor() {
    }

    public ExtensibleTypeDescriptor(String name) {
        super();
        this.name = name;
    }

    public ExtensibleTypeDescriptor(String name, String supertype) {
        super();
        this.name = name;
        this.supertype = supertype;
    }


    public FieldDescriptor addField(String name, FieldDescriptor.Type type) {
        return addField(new FieldDescriptor(name, type));
    }

    public FieldDescriptor addField(FieldDescriptor field) {
        this.fields.add(field);
        return field;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupertype() {
        return supertype;
    }

    public void setSupertype(String supertype) {
        this.supertype = supertype;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public DateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(DateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Set<FieldDescriptor> getFields() {
        return fields;
    }

}
