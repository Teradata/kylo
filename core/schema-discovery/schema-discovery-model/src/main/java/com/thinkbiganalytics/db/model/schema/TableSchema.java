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

    private String delim;

    private String escape;

    private String quote;

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

    public String getDelim() {
        return delim;
    }

    public void setDelim(String delim) {
        this.delim = delim;
    }

    public String getEscape() {
        return escape;
    }

    public void setEscape(String escape) {
        this.escape = escape;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @JsonIgnore
    public String deriveHiveRecordFormat() {

        String template = "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'\n" +
                          " WITH SERDEPROPERTIES (" +
                          deriveSeparatorRecordFormat() +
                          deriveEscapeCharRecordFormat() +
                          deriveQuoteRecordFormat() +
                          ") STORED AS TEXTFILE";
        hiveRecordFormat = String.format(template);
        System.out.println(hiveRecordFormat);
        return hiveRecordFormat;
    }

    private String deriveSeparatorRecordFormat() {
        String template = " \"separatorChar\" = \"%s\"";
        return String.format(template, getDelim());
    }

    private String deriveQuoteRecordFormat() {
        if (quote == null) {
            return "";
        }
        String template = " ,\"quoteChar\" = \"%s\"";
        return String.format(template, getQuote());
    }

    private String deriveEscapeCharRecordFormat() {
        if (escape == null) {
            return "";
        }
        String template = " ,\"escapeChar\" = \"%s\"";
        return String.format(template, getEscape());
    }


    public String getHiveRecordFormat() {
        return hiveRecordFormat;
    }

    public void setHiveRecordFormat(String hiveRecordFormat) {
        this.hiveRecordFormat = hiveRecordFormat;
    }

    @JsonIgnore
    public Map<String, Field> getFieldsAsMap() {
        Map<String, Field> map = new HashMap<>();
        if (fields != null) {
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
