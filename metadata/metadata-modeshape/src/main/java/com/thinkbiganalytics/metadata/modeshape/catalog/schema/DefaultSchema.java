package com.thinkbiganalytics.metadata.modeshape.catalog.schema;

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
