package com.thinkbiganalytics.support;

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
 * Created by sr186054 on 1/25/16.
 */
public class AnnotationFieldNameResolver {

    private Set<Class> processedClasses = new HashSet<>();
    private Map<Class,Set<AnnotatedFieldProperty>> classPropertyFields = new HashMap<>();
    private Stack<String> stack = new Stack<>();
    private String parentPrefix = "";
    private Class<? extends Annotation>annotation;
    public AnnotationFieldNameResolver(String parentPrefix, Class<? extends Annotation> annotation){
        this.parentPrefix = parentPrefix;
        if(StringUtils.isNotBlank(this.parentPrefix) && this.parentPrefix.endsWith(".")){
            this.parentPrefix = StringUtils.substringBeforeLast(this.parentPrefix,".");
        }
        this.annotation = annotation;
    }
    public AnnotationFieldNameResolver(Class<? extends Annotation> annotation){
        this.annotation = annotation;
    }

    public void afterFieldNameAdded(Class clazz,String classBeanPrefix,List<AnnotatedFieldProperty> names, Field field) {

    }

    public String getFieldPropertyDescription(Field field ){
        return null;
    }

    public AnnotatedFieldProperty addFieldProperty( Class clazz,List<AnnotatedFieldProperty> names, Field field){
        AnnotatedFieldProperty annotatedFieldProperty = new AnnotatedFieldProperty();
        annotatedFieldProperty.setName(stackAsString() + field.getName());
        //annotatedFieldProperty.setFieldName(field.getName());
        annotatedFieldProperty.setField(field);
        annotatedFieldProperty.setDescription(getFieldPropertyDescription(field));
        names.add(annotatedFieldProperty);
        afterFieldNameAdded(clazz, stackAsString(), names, field);
        return annotatedFieldProperty;
    }


    public  List<AnnotatedFieldProperty> getProperties(Class clazz){
        processedClasses.add(clazz);
        classPropertyFields.put(clazz, new HashSet<AnnotatedFieldProperty>());
        List<AnnotatedFieldProperty> names = new ArrayList<>();
        List<Field> fields =  FieldUtils.getFieldsListWithAnnotation(clazz, annotation);
        List<Field> allFields = FieldUtils.getAllFieldsList(clazz);
        for (Field field: fields){
            AnnotatedFieldProperty p = addFieldProperty(clazz,names,field);
            classPropertyFields.get(clazz).add(p);
            Class fieldType = field.getType();
            /*
            if(fieldType.isAssignableFrom(List.class)) {
                ParameterizedType t = (ParameterizedType) field.getGenericType();
                if(t != null && t.getActualTypeArguments() != null && t.getActualTypeArguments().length >0) {
                    try {
                        fieldType =  Class.forName(t.getActualTypeArguments()[0].getTypeName());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            */
            if(!processedClasses.contains(fieldType)){
                names.addAll(getProperties(fieldType));
            }
        }
        for(Field field: allFields){
            Class fieldType = field.getType();
            if(!processedClasses.contains(fieldType)){
                stack.push(field.getName());
                names.addAll(getProperties(fieldType));
                stack.pop();
            }
            else if(classPropertyFields.containsKey(fieldType)) {
                stack.push(field.getName());
                for(AnnotatedFieldProperty prop: classPropertyFields.get(fieldType)){
                    addFieldProperty(clazz,names,prop.getField());
                }
                stack.pop();
            }
        }
        return names;
    }

    private String stackAsString(){
        String str = "";
        if(StringUtils.isNotBlank(parentPrefix)){
            str +=parentPrefix+(stack.isEmpty()?".":"");
        }
        if(stack != null && !stack.isEmpty()){
            for(String item:stack){
                if(StringUtils.isNotBlank(str)){
                    str +=".";
                }
                str +=item;
            }
            if(StringUtils.isNotBlank(str)){
                str +=".";
            }
        }

        return str;
    }
}
