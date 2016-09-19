/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;

import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AbstractSchema implements Schema {

    private UUID uuid = UUID.randomUUID();

    private String name;

    private String description;

    private String charset;

    private Map<String, String> properties = new HashMap<>();

    private List<? extends Field> fields = new ArrayList<>();


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
    public List<? extends Field> getFields() {
        return fields;
    }

    public void setFields(List<? extends Field> fields) {
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
