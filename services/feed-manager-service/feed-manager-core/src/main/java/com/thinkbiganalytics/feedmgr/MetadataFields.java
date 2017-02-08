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
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;

import java.util.List;

/**
 * Utiltiy to inspect a class or {@link FeedMetadata} object and return information about all the fields annotated with {@link com.thinkbiganalytics.metadata.MetadataField}
 * Fields annotated with {@link com.thinkbiganalytics.metadata.MetadataField} are accessible when registering a template in Kylo and can be injected into a NiFi flow when the feed is created.
 */
public class MetadataFields {


    private List<AnnotatedFieldProperty> properties = null;

    private MetadataFieldAnnotationFieldNameResolver resolver;

    private MetadataFields() {
        resolver = new MetadataFieldAnnotationFieldNameResolver();
    }

    public static MetadataFields getInstance() {
        return LazyHolder.INSTANCE;
    }

    public List<AnnotatedFieldProperty> getProperties(Class clazz) {
        if (properties == null) {
            properties = resolver.getProperties(clazz);
        }
        return properties;

    }

    public String getMatchingPropertyDescriptor(FeedMetadata feedMetadata, String propertyDescriptor) {
        if (properties == null) {
            getProperties(FeedMetadata.class);
        }
        return resolver.getMatchingPropertyDescriptor(propertyDescriptor);
    }

    private static class LazyHolder {

        static final MetadataFields INSTANCE = new MetadataFields();
    }


}
