package com.thinkbiganalytics.annotations;

/*-
 * #%L
 * thinkbig-commons-util
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

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Class used to extract field annotations and return as an object describing the annotation and its purpose as well as the Field data
 * A helper class is usually used in conjunction with this class to build up this object parsing Field annotations for a given object/class
 *
 * @see AnnotationFieldNameResolver
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

    /**
     * Returns the name of the field or the dot notation of a field if its derived as a child element from a parent class
     *
     * @return the name of the field.  When used with the {@link AnnotationFieldNameResolver} it will resolve to the name of the field as it pertains to the parent object separated by a "."
     */
    public String getName() {
        return name;
    }

    /**
     * Set the field name used for this annotation.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return an optional description about the field
     *
     * @return the description for the field.  This is optional and can be null
     */
    public String getDescription() {
        return description;
    }

    /**
     * set the description for the field
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the Java Field reference
     *
     * @return the Java Field reference to the annotated field
     */
    public Field getField() {
        return field;
    }

    /**
     * set the field containing the annotation
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * Return the Java datatype of the field
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * set the Java datatype
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Return the Annotation used when parsing this field
     */
    public T getAnnotation() {
        return annotation;
    }

    /**
     * set the annotation reference
     */
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
