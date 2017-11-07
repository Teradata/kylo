package com.thinkbiganalytics.feedmgr;

/*-
 * #%L
 * thinkbig-feed-manager-core
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


import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;
import com.thinkbiganalytics.annotations.AnnotationFieldNameResolver;
import com.thinkbiganalytics.metadata.MetadataField;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to inspect any object/class looking for {@link MetadataField} annotations on fields and return information about that field and the annotation
 */
public class MetadataFieldAnnotationFieldNameResolver extends AnnotationFieldNameResolver {

    public static final String metadataPropertyPrefix = "metadata.";

    private Map<String, String> matchingPropertyMap = new HashMap<>();

    public MetadataFieldAnnotationFieldNameResolver() {
        super(metadataPropertyPrefix, MetadataField.class);
        //add in known matching properties

        //The property ${metadata.table.existingTableName} is now found at $metadata.table.sourceTableSchema.name}
        matchingPropertyMap.put(metadataPropertyPrefix + "table.sourceTableSchema.name", metadataPropertyPrefix + "table.existingTableName");


    }

    @Override
    public void afterFieldNameAdded(Class clazz, String classBeanPrefix, List<AnnotatedFieldProperty> names, Field field) {
        MetadataField annotation = field.getAnnotation(MetadataField.class);
        if (annotation.matchingFields().length > 0) {
            for (String matchingField : annotation.matchingFields()) {
                matchingPropertyMap.put(classBeanPrefix + field.getName(), classBeanPrefix + matchingField);
            }
        }
    }


    @Override
    public String getFieldPropertyDescription(Field field) {
        MetadataField metadataField = field.getAnnotation(MetadataField.class);
        if (metadataField != null) {
            return metadataField.description();
        }
        return null;
    }

    public String getMatchingPropertyDescriptor(String propertyDescriptor) {
        return matchingPropertyMap.get(propertyDescriptor);
    }


}
