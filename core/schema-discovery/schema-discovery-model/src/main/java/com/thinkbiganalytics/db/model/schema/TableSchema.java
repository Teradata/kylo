/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.db.model.schema;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.thinkbiganalytics.feedmgr.metadata.MetadataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TableSchema {


    @MetadataField
    private String name;

    private String schemaName;

    private String description = "";

    public List<Field> fields;

    private Character delim;

    private boolean escapes;

    private boolean quotes;

    private String hiveRecordFormat;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public Character getDelim() {
        return delim;
    }

    public void setDelim(Character delim) {
        this.delim = delim;
    }

    public boolean isEscapes() {
        return escapes;
    }

    public void setEscapes(boolean escapes) {
        this.escapes = escapes;
    }

    public boolean isQuotes() {
        return quotes;
    }

    public void setQuotes(boolean quotes) {
        this.quotes = quotes;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @JsonIgnore
    public String deriveHiveRecordFormat(){
        hiveRecordFormat = "ROW FORMAT DELIMITED FIELDS TERMINATED BY '"+getDelim()+"' LINES TERMINATED BY '\\n' STORED AS TEXTFILE";
        return hiveRecordFormat;
    }

    public String getHiveRecordFormat() {
        return hiveRecordFormat;
    }

    public void setHiveRecordFormat(String hiveRecordFormat) {
        this.hiveRecordFormat = hiveRecordFormat;
    }

    @JsonIgnore
    public Map<String,Field> getFieldsAsMap(){
        Map<String,Field> map = new HashMap<>();
        if(fields != null) {
            map = Maps.uniqueIndex(fields, new Function<Field, String>() {
                @Override
                public String apply(Field field) {
                    return field.getName();
                }
            });

        }
        return map;
    }
}
