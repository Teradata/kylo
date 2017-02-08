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

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;
import com.thinkbiganalytics.nifi.core.api.metadata.WaterMarkActiveException;
import com.thinkbiganalytics.nifi.v2.common.CommonProperties;
import com.thinkbiganalytics.nifi.v2.common.FeedIdNotFoundException;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loads a high-water mark and yields any processing if the water mark has not been released yet.
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"load", "high-water", "mark", "thinkbig"})
@CapabilityDescription("Loads and makes active a watermark associated with a feed.")
public class LoadHighWaterMark extends HighWaterMarkProcessor {

    public static final Relationship ACTIVE_FAILURE = new Relationship.Builder()
        .name("activeFailure")
        .description("The water mark is actively being processed and has not yet been committed or rejected")
        .build();
    protected static final AllowableValue[] ACTIVE_STRATEGY_VALUES = new AllowableValue[]{
        new AllowableValue("YIELD", "Yield", "Yield processing so that another attempt to obtain the high-water mark can be made later."),
        new AllowableValue("PENALIZE", "Penalize", "Penalize the flow file and return it to the queue that it came from so that another "
                                                   + "attempt to obtain the high-water mark can be made later.  Performs a yield instead if this processor is the first in the flow."),
        new AllowableValue("ROUTE", "Route", "Route immediately to the \"activeFailure\" relationship.")
    };
    protected static final PropertyDescriptor ACTIVE_WATER_MARK_STRATEGY = new PropertyDescriptor.Builder()
        .name("Active Water Mark Strategy")
        .description("Specifies what strategy should be followed when an attempt to obtain the latest high-water mark fails because another "
                     + "is flow already actively using it")
        .allowableValues(ACTIVE_STRATEGY_VALUES)
        .defaultValue("YIELD")
        .required(true)
        .build();
    protected static final PropertyDescriptor MAX_YIELD_COUNT = new PropertyDescriptor.Builder()
        .name("Max Yield Count")
        .description("The maximum number of yields, if the yield strategy is selected, "
                     + "that should be attempted before failures to obtain a high-water mark are routed to the \"active\" relationship "
                     + "(routing never occurs if unset or less than zero)")
        .required(false)
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    protected static final PropertyDescriptor INITIAL_VALUE = new PropertyDescriptor.Builder()
        .name("Initial Value")
        .description("The initial value for the water mark if none currently exists for the feed.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    private AtomicInteger yieldCount = new AtomicInteger(0);

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile inputFF = session.get();
        FlowFile outputFF = inputFF;
        boolean createdFlowfile = false;

        // Create the flow file if we are the start of the flow.
        if (outputFF == null && !context.hasNonLoopConnection()) {
            outputFF = session.create();
            createdFlowfile = true;
        }

        if (outputFF != null) {
            try {
                outputFF = initialize(context, session, outputFF);
            } catch (FeedIdNotFoundException e) {
                // Initialize should find the current feed ID.  If we are the head of the flow then the feed metadata 
                // creation may not have completed before we were triggered we will yield so that we can retry.  
                // Otherwise re-throw the exception.
                if (createdFlowfile && outputFF.getAttribute(FEED_ID_ATTR) == null) {
                    getLog().debug("ID for feed was not available yet - yielding");
                    session.remove(outputFF);
                    context.yield();
                    return;
                } else {
                    throw e;
                }
            }

            MetadataRecorder recorder = context.getProperty(CommonProperties.METADATA_SERVICE).asControllerService(MetadataProviderService.class).getRecorder();
            String waterMark = context.getProperty(HIGH_WATER_MARK).getValue();
            String propName = context.getProperty(PROPERTY_NAME).getValue();
            String initialValue = context.getProperty(INITIAL_VALUE).getValue();

            try {
                String feedId = getFeedId(context, outputFF);

                try {
                    outputFF = recorder.loadWaterMark(session, outputFF, feedId, waterMark, propName, initialValue);
                } catch (WaterMarkActiveException e) {
                    throw e;
                } catch (Exception e) {
                    getLog().error("Failed to load the current high-water mark: {} for feed {}", new Object[]{waterMark, feedId}, e);
                    session.transfer(outputFF, CommonProperties.REL_FAILURE);
                }

                this.yieldCount.set(0);
                session.transfer(outputFF, CommonProperties.REL_SUCCESS);
            } catch (WaterMarkActiveException e) {
                String strategy = context.getProperty(ACTIVE_WATER_MARK_STRATEGY).getValue();

                if ("ROUTE".equals(strategy)) {
                    getLog().debug("Water mark {} is active - routing to \"activeFailure\"", new Object[]{waterMark});
                    session.transfer(outputFF, ACTIVE_FAILURE);
                } else {
                    PropertyValue value = context.getProperty(MAX_YIELD_COUNT);
                    int maxCount = value.isSet() ? value.asInteger() : Integer.MAX_VALUE - 1;
                    int count = this.yieldCount.incrementAndGet();

                    if (maxCount > 0 && count > maxCount) {
                        getLog().debug("Water mark {} is active - routing to \"activeFailure\"", new Object[]{waterMark});
                        session.transfer(outputFF, ACTIVE_FAILURE);
                    } else {
                        // If this processor created this flow file (1st processor in flow) then we will yield no matter what the strategy.
                        if (createdFlowfile) {
                            getLog().debug("Removing creatd flow file and yielding because water mark {} is active - attempt {} of {}", new Object[]{waterMark, count, maxCount});
                            session.remove(outputFF);
                            context.yield();
                        } else {
                            if ("YIELD".equals(strategy)) {
                                getLog().debug("Yielding because water mark {} is active - attempt {} of {}", new Object[]{waterMark, count, maxCount});
                                session.transfer(outputFF);
                                context.yield();
                            } else {
                                getLog().debug("Penalizing flow file because water mark {} is active - attempt {} of {}", new Object[]{waterMark, count, maxCount});
                                outputFF = session.penalize(outputFF);
                                session.transfer(outputFF);
                            }
                        }
                    }
                }
            }
        }

    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.v2.common.BaseProcessor#addProperties(java.util.List)
     */
    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(ACTIVE_WATER_MARK_STRATEGY);
        list.add(MAX_YIELD_COUNT);
        list.add(INITIAL_VALUE);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.v2.common.BaseProcessor#addRelationships(java.util.Set)
     */
    @Override
    protected void addRelationships(Set<Relationship> set) {
        super.addRelationships(set);
        set.add(ACTIVE_FAILURE);
    }
}
