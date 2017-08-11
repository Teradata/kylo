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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Walk a Class with Field annotations and create {@link AnnotatedFieldProperty} objects describing the annotation
 */
public class AnnotationFieldNameResolver {

    private Set<Class> processedClasses = new HashSet<>();
    private Map<Class, Set<AnnotatedFieldProperty>> classPropertyFields = new HashMap<>();
    private Stack<String> stack = new Stack<>();
    private String parentPrefix = "";
    private Class<? extends Annotation> annotation;

    public AnnotationFieldNameResolver(String parentPrefix, Class<? extends Annotation> annotation) {
        this.parentPrefix = parentPrefix;
        if (StringUtils.isNotBlank(this.parentPrefix) && this.parentPrefix.endsWith(".")) {
            this.parentPrefix = StringUtils.substringBeforeLast(this.parentPrefix, ".");
        }
        this.annotation = annotation;
    }

    public AnnotationFieldNameResolver(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    public void afterFieldNameAdded(Class clazz, String classBeanPrefix, List<AnnotatedFieldProperty> names, Field field) {

    }

    /**
     * obtain a description for the field.
     *
     * @return the description of the field or null.
     */
    public String getFieldPropertyDescription(Field field) {
        return null;
    }

    /**
     * create the object describing the Field annotation setting the name based upon the class hierarchy
     *
     * @param clazz the class to inspect
     * @param names the list of names already parsed.  this will be the list the newly parsed field is added to
     * @param field the field to parse for the annotation matching the {@link #annotation} supplied
     * @return the object describing the annotation
     * @see #stackAsString() for the naming strategy on hierachy names
     */
    public AnnotatedFieldProperty addFieldProperty(Class clazz, List<AnnotatedFieldProperty> names, Field field) {
        AnnotatedFieldProperty annotatedFieldProperty = new AnnotatedFieldProperty();
        annotatedFieldProperty.setAnnotation(field.getAnnotation(annotation));

        annotatedFieldProperty.setName(stackAsString() + field.getName());
        annotatedFieldProperty.setField(field);
        annotatedFieldProperty.setDescription(getFieldPropertyDescription(field));
        names.add(annotatedFieldProperty);
        afterFieldNameAdded(clazz, stackAsString(), names, field);
        return annotatedFieldProperty;
    }


    /**
     * Walk a class and obtain {@link AnnotatedFieldProperty} objects matching any fields with the {@link #annotation} supplied
     *
     * @param clazz the class to inspect and parse annotations
     * @return a list of objects describing the annotated fields
     */
    public List<AnnotatedFieldProperty> getProperties(Class clazz) {
        processedClasses.add(clazz);
        classPropertyFields.put(clazz, new HashSet<AnnotatedFieldProperty>());
        List<AnnotatedFieldProperty> names = new ArrayList<>();
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(clazz, annotation);
        List<Field> allFields = FieldUtils.getAllFieldsList(clazz);
        for (Field field : fields) {
            AnnotatedFieldProperty p = addFieldProperty(clazz, names, field);
            classPropertyFields.get(clazz).add(p);
            Class fieldType = field.getType();

            if (!processedClasses.contains(fieldType)) {
                names.addAll(getProperties(fieldType));
            }
        }

        for (Field field : allFields) {
            Class fieldType = field.getType();
            if (!processedClasses.contains(fieldType)) {
                stack.push(field.getName());
                names.addAll(getProperties(fieldType));
                //check to see if field is annotated with deserialize
                JsonDeserialize deserialize = field.getAnnotation(JsonDeserialize.class);
                if (deserialize != null) {
                    Class<?> deserializeClass = deserialize.as();
                    if (!processedClasses.contains(deserializeClass)) {
                        names.addAll(getProperties(deserializeClass));
                    }
                }

                stack.pop();
            } else if (classPropertyFields.containsKey(fieldType)) {
                stack.push(field.getName());
                for (AnnotatedFieldProperty prop : classPropertyFields.get(fieldType)) {
                    addFieldProperty(clazz, names, prop.getField());
                }
                //check to see if field is annotated with deserialize
                JsonDeserialize deserialize = field.getAnnotation(JsonDeserialize.class);
                if (deserialize != null) {
                    Class<?> deserializeClass = deserialize.as();
                    if (classPropertyFields.containsKey(deserializeClass)) {
                        for (AnnotatedFieldProperty prop : classPropertyFields.get(deserializeClass)) {
                            addFieldProperty(clazz, names, prop.getField());
                        }
                    }
                }

                stack.pop();
            }
        }
        return names;
    }

    /**
     * create a name separated by "." based on a fields class hierarchy
     */
    private String stackAsString() {
        String str = "";
        if (StringUtils.isNotBlank(parentPrefix)) {
            str += parentPrefix + (stack.isEmpty() ? "." : "");
        }
        if (stack != null && !stack.isEmpty()) {
            for (String item : stack) {
                if (StringUtils.isNotBlank(str)) {
                    str += ".";
                }
                str += item;
            }
            if (StringUtils.isNotBlank(str)) {
                str += ".";
            }
        }

        return str;
    }
}
