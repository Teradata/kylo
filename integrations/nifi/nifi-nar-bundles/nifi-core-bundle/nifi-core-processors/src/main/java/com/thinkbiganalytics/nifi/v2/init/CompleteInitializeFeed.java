/**
 *
 */
package com.thinkbiganalytics.nifi.v2.init;

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

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import java.util.List;
import java.util.Set;

/**
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "initialize", "initialization", "thinkbig"})
@CapabilityDescription("Indicates that a feed initialization flow has completed; either successfully or a failed")
public class CompleteInitializeFeed extends FeedProcessor {

    protected static final AllowableValue[] MODE_VALUES = new AllowableValue[]{
        new AllowableValue("SUCCESSFUL", "Successful", "The mode indicating feed initialization was successful."),
        new AllowableValue("FAILURE", "Failure", "The mode indicating feed initialization failed.")
    };

    protected static final PropertyDescriptor FAILURE_STRATEGY = new PropertyDescriptor.Builder()
        .name("Initialization Result")
        .description("Indicates how this processor should behave when a flow file arrives after feed initialization has failed.")
        .allowableValues(MODE_VALUES)
        .required(true)
        .build();

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile inputFF = session.get();
        if (inputFF != null) {
            inputFF = initialize(context, session, inputFF);
            String mode = context.getProperty(FAILURE_STRATEGY).getValue();

            if ("SUCCESSFUL".equals(mode)) {
                getMetadataRecorder().completeFeedInitialization(getFeedId(context, inputFF));
            } else {
                getMetadataRecorder().failFeedInitialization(getFeedId(context, inputFF));
            }

            session.transfer(inputFF, CommonProperties.REL_SUCCESS);
        }
    }

    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(FAILURE_STRATEGY);
    }

    @Override
    protected void addRelationships(Set<Relationship> set) {
        super.addRelationships(set);
        set.add(CommonProperties.REL_SUCCESS);
        set.add(CommonProperties.REL_FAILURE);
    }

}
