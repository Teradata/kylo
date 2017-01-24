package com.thinkbiganalytics.nifi.v2.common;

import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;

/**
 * Common properties shared by many processors.
 */
public interface CommonProperties {
    
    AllowableValue[] BOOLEANS = new AllowableValue[] { new AllowableValue("true", "True"), new AllowableValue("false", "False") };
    AllowableValue[] ENABLING = new AllowableValue[] { new AllowableValue("true", "Enabled"), new AllowableValue("false", "Disabled") };
    
    /**
     * Common Controller services
     **/
    PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
        .name("Metadata Service")
        .description("Think Big metadata service")
        .required(true)
        .identifiesControllerService(MetadataProviderService.class)
        .build();

    /**
     * Common component properties
     **/

    PropertyDescriptor FEED_CATEGORY = new PropertyDescriptor.Builder()
        .name("System feed category")
        .description("System category of the feed this processor supports")
        .required(true)
        .defaultValue("${metadata.category.systemName}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
        .name("System feed name")
        .description("Name of feed this processor supports")
        .defaultValue("${metadata.systemFeedName}")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    // Standard Relationships
    Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Processing was successful")
        .build();

    Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("Processing failed")
        .build();


}
