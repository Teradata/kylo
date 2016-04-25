package com.thinkbiganalytics.annotations;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Created by sr186054 on 4/4/16.
 */
public class AnnotatedFieldProperty<T extends Annotation> {
    private String name;
    private String description;
    @JsonIgnore
    private Field field;
    private String dataType;

    private T annotation;


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

    public T getAnnotation() {
        return annotation;
    }

    public void setAnnotation(T annotation) {
        this.annotation = annotation;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
             .append("name", name)
            .append("description", description)
            .toString();
    }
}
