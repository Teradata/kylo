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

import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus.State;
import com.thinkbiganalytics.nifi.v2.common.CommonProperties;
import com.thinkbiganalytics.nifi.v2.common.FeedProcessor;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "initialize", "initialization", "thinkbig"})
@CapabilityDescription("Controls setup of a feed by routing to an initialization flow.")
public class InitializeFeed extends FeedProcessor {

    protected static final AllowableValue[] FAIL_STRATEGY_VALUES = new AllowableValue[]{
        new AllowableValue("FAIL", "Fail", "Immediately fail the flow file"),
        new AllowableValue("RETRY", "Retry", "Retry initialization (if the appropriate time delay has expired) and penalize the flow file.")
    };

    protected static final PropertyDescriptor FAILURE_STRATEGY = new PropertyDescriptor.Builder()
        .name("Initialization Failure Strategy")
        .description("Indicates how this processor should behave when a flow file arrives after feed initialization has failed.")
        .allowableValues(FAIL_STRATEGY_VALUES)
        .defaultValue("RETRY")
        .required(true)
        .build();

    protected static final PropertyDescriptor RETRY_DELAY = new PropertyDescriptor.Builder()
        .name("Initialization Retry Delay (seconds)")
        .description("The minimum amount of seconds to delay before an arriving flow file should trigger another attempt to "
                     + "initialize a feed that has previously failed initialization.  Any flow file arriving before this "
                     + "delay has expired will be immediately failed.")
        .required(false)
        .defaultValue("60")
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    protected static final PropertyDescriptor MAX_INIT_ATTEMPTS = new PropertyDescriptor.Builder()
        .name("Max Initialization Attempts")
        .description("The maximum number of times initialization will be retried where there are failures.  There is no limit if unset.")
        .required(true)
        .defaultValue("5")
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    Relationship REL_INITIALIZE = new Relationship.Builder()
        .name("Initialize")
        .description("Begin initialization")
        .build();

    private Map<String, AtomicInteger> retryCounts = Collections.synchronizedMap(new HashMap<>());

    @OnScheduled
    public void scheduled(ProcessContext context) {
        super.scheduled(context);
        this.retryCounts.clear();
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile inputFF = session.get();
        if (inputFF != null) {
            inputFF = initialize(context, session, inputFF);
            InitializationStatus status = getMetadataRecorder().getInitializationStatus(getFeedId(context, inputFF))
                .orElse(new InitializationStatus(State.PENDING));

            switch (status.getState()) {
                case PENDING:
                    pending(context, session, inputFF);
                    break;
                case IN_PROGRESS:
                    inProgress(context, session, inputFF);
                    break;
                case FAILED:
                    failed(context, session, inputFF, status.getTimestamp());
                    break;
                default:
                    success(context, session, inputFF);
            }
        }
    }

    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(FAILURE_STRATEGY);
        list.add(RETRY_DELAY);
        list.add(MAX_INIT_ATTEMPTS);
    }

    @Override
    protected void addRelationships(Set<Relationship> set) {
        super.addRelationships(set);
        set.add(CommonProperties.REL_SUCCESS);
        set.add(CommonProperties.REL_FAILURE);
        set.add(REL_INITIALIZE);
    }

    private void pending(ProcessContext context, ProcessSession session, FlowFile inputFF) {
        beginInitialization(context, session, inputFF);
        rejectFlowFile(session, inputFF);
    }

    private void inProgress(ProcessContext context, ProcessSession session, FlowFile inputFF) {
        rejectFlowFile(session, inputFF);
    }

    private void failed(ProcessContext context, ProcessSession session, FlowFile inputFF, DateTime failTime) {
        String strategy = context.getProperty(FAILURE_STRATEGY).getValue();

        if (strategy.equals("RETRY")) {
            int delay = context.getProperty(RETRY_DELAY).asInteger();
            int max = context.getProperty(MAX_INIT_ATTEMPTS).asInteger();
            AtomicInteger count = getRetryCount(context, inputFF);

            if (count.getAndIncrement() >= max) {
                count.set(max);
                session.transfer(inputFF, CommonProperties.REL_FAILURE);
            } else if (failTime.plusSeconds(delay).isBefore(DateTime.now(DateTimeZone.UTC))) {
                beginInitialization(context, session, inputFF);
                rejectFlowFile(session, inputFF);
            } else {
                session.transfer(inputFF, CommonProperties.REL_FAILURE);
            }
        } else {
            session.transfer(inputFF, CommonProperties.REL_FAILURE);
        }
    }

    private void success(ProcessContext context, ProcessSession session, FlowFile inputFF) {
        session.transfer(inputFF, CommonProperties.REL_SUCCESS);
    }

    private void beginInitialization(ProcessContext context, ProcessSession session, FlowFile inputFF) {
        getMetadataRecorder().startFeedInitialization(getFeedId(context, inputFF));
        FlowFile initFF = session.create(inputFF);
        session.transfer(initFF, REL_INITIALIZE);
    }

    private void rejectFlowFile(ProcessSession session, FlowFile inputFF) {
        FlowFile penalizedFF = session.penalize(inputFF);
        session.transfer(penalizedFF);
    }

    private AtomicInteger getRetryCount(ProcessContext context, FlowFile inputFF) {
        return this.retryCounts.computeIfAbsent(getFeedId(context, inputFF), k -> new AtomicInteger(0));
    }

}
