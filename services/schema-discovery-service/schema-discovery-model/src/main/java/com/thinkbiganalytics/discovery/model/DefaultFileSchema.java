/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.FileSchema;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultFileSchema extends AbstractSchema implements FileSchema {

    private String format;
    private boolean binary;
    private boolean embeddedSchema;

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public boolean isBinary() {
        return binary;
    }

    @Override
    public boolean hasEmbeddedSchema() {
        return embeddedSchema;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public boolean isEmbeddedSchema() {
        return embeddedSchema;
    }

    public void setEmbeddedSchema(boolean embeddedSchema) {
        this.embeddedSchema = embeddedSchema;
    }
}
