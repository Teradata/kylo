/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
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

    @JsonDeserialize(contentAs=DefaultField.class)
    @JsonSerialize(contentAs=DefaultField.class)
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
    public String getDescription() {
        return description;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public String getSchemaName() {
        return this.schemaName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCharset(String charset) {
        this.charset = charset;
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
