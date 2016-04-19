package com.thinkbiganalytics.feedmgr;


import com.thinkbiganalytics.feedmgr.metadata.MetadataField;
import com.thinkbiganalytics.support.AnnotatedFieldProperty;
import com.thinkbiganalytics.support.AnnotationFieldNameResolver;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 4/4/16.
 */
public class MetadataFieldAnnotationFieldNameResolver extends AnnotationFieldNameResolver {

    public static String metadataPropertyPrefix = "metadata.";

    private Map<String,String> matchingPropertyMap = new HashMap<>();
    public MetadataFieldAnnotationFieldNameResolver() {
        super(metadataPropertyPrefix, MetadataField.class);
        //add in known matching properties

        //The property ${metadata.table.existingTableName} is now found at $metadata.table.sourceTableSchema.name}
        matchingPropertyMap.put(metadataPropertyPrefix + "table.sourceTableSchema.name", metadataPropertyPrefix + "table.existingTableName");


    }

    @Override
    public void afterFieldNameAdded(Class clazz,String classBeanPrefix,List<AnnotatedFieldProperty> names, Field field) {
        MetadataField annotation = field.getAnnotation(MetadataField.class);
        if(annotation.matchingFields().length >0){
            for(String matchingField: annotation.matchingFields()) {
                matchingPropertyMap.put(classBeanPrefix + field.getName(), classBeanPrefix + matchingField);
            }
        }
    }


    @Override
    public String getFieldPropertyDescription(Field field) {
        MetadataField metadataField = field.getAnnotation(MetadataField.class);
        if(metadataField != null){
            return metadataField.description();
        }
        return null;
    }

    public String getMatchingPropertyDescriptor(String propertyDescriptor){
        return matchingPropertyMap.get(propertyDescriptor);
    }


}
