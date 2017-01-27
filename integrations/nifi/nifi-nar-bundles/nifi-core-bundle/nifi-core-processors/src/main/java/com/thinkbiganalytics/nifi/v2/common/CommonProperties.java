package com.thinkbiganalytics.nifi.v2.common;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;

import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

/**
 * Common properties shared by many processors.
 */
public interface CommonProperties {

    AllowableValue[] BOOLEANS = new AllowableValue[]{new AllowableValue("true", "True"), new AllowableValue("false", "False")};
    AllowableValue[] ENABLING = new AllowableValue[]{new AllowableValue("true", "Enabled"), new AllowableValue("false", "Disabled")};

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
