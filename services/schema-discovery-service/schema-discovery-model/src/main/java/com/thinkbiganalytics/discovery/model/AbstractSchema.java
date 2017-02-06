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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.Schema;
import com.thinkbiganalytics.metadata.MetadataField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

public class AbstractSchema implements Schema {

    private UUID uuid = UUID.randomUUID();

    @MetadataField
    private String name;

    private String description;

    private String charset;

    private Map<String, String> properties = new HashMap<>();

    @JsonDeserialize(contentAs = DefaultField.class)
    @JsonSerialize(contentAs = DefaultField.class)
    private List<Field> fields = new ArrayList<>();

    private String schemaName;


    @Override
    public UUID getID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @JsonIgnore
    public Map<String, Field> getFieldsAsMap() {
        Map<String, Field> map = new HashMap<>();
        if (fields != null) {
            map = Maps.uniqueIndex(fields, new Function<Field, String>() {
                @Nullable
                @Override
                public String apply(@Nullable Field field) {
                    return field.getName();
                }
            });
        }
        return map;
    }
}
