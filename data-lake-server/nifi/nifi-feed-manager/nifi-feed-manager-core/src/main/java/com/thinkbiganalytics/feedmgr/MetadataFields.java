package com.thinkbiganalytics.feedmgr;

import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;

import java.util.List;

/**
 * Created by sr186054 on 4/4/16.
 */
public class MetadataFields {


    private List<AnnotatedFieldProperty> properties = null;

    private MetadataFieldAnnotationFieldNameResolver resolver;
    private static class LazyHolder {
        static final MetadataFields INSTANCE = new MetadataFields();
    }

    public static MetadataFields getInstance() {
        return LazyHolder.INSTANCE;
    }

    private MetadataFields(){
        resolver = new MetadataFieldAnnotationFieldNameResolver();
    }

    public List<AnnotatedFieldProperty> getProperties(Class clazz){
        if(properties == null) {
            properties = resolver.getProperties(clazz);
        }
        return properties;

    }

    public String getMatchingPropertyDescriptor(FeedMetadata feedMetadata, String propertyDescriptor){
        if(properties == null){
            getProperties(FeedMetadata.class);
        }
        return resolver.getMatchingPropertyDescriptor(propertyDescriptor);
    }



}
