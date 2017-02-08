/**
 *
 */
package com.thinkbiganalytics.nifi.v2.core.watermark;

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

import com.thinkbiganalytics.nifi.v2.common.CommonProperties;
import com.thinkbiganalytics.nifi.v2.common.FeedProcessor;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.List;
import java.util.Set;

/**
 * Base abstract processor for high-water mark processors.
 */
public abstract class HighWaterMarkProcessor extends FeedProcessor {

    protected static final PropertyDescriptor HIGH_WATER_MARK = new PropertyDescriptor.Builder()
        .name("High-Water Mark")
        .description("The name to be given to this high-water mark, stored in the feed's metadata, which records the latest committed water mark value")
        .defaultValue("highWaterMark")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    protected static final PropertyDescriptor PROPERTY_NAME = new PropertyDescriptor.Builder()
        .name("High-Water Mark Value Property Name")
        .description("Name of the flow file property which is set to the current high-water mark value for use in "
                     + "subsequent processing and commit")
        .defaultValue("water.mark")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(HIGH_WATER_MARK);
        list.add(PROPERTY_NAME);
    }

    @Override
    protected void addRelationships(Set<Relationship> set) {
        super.addRelationships(set);
        set.add(CommonProperties.REL_SUCCESS);
        set.add(CommonProperties.REL_FAILURE);
    }
}
