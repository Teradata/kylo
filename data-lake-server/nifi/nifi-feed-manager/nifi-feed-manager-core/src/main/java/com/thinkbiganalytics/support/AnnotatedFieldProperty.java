package com.thinkbiganalytics.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;

import java.lang.reflect.Field;

/**
 * Created by sr186054 on 4/4/16.
 */
public class AnnotatedFieldProperty {
    private String name;
    private String description;
    @JsonIgnore
    private Field field;
    private String dataType;

    public AnnotatedFieldProperty() {

    }

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

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("description", description)
                .add("name", name)
                .toString();
    }


}
