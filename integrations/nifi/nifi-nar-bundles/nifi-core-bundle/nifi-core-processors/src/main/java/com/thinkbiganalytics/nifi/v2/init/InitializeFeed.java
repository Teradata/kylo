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
import org.apache.nifi.expression.AttributeExpression;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.DataUnit;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.FlowFileFilters;
import org.apache.nifi.processor.util.StandardValidators;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "initialize", "initialization", "thinkbig"})
@CapabilityDescription("Controls setup of a feed by routing to an initialization or re-initialization flow.")
public class InitializeFeed extends FeedProcessor {

    public static final String REINITIALIZING_FLAG = "reinitializing";

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

    protected static final PropertyDescriptor CLONE_INIT_FLOWFILE = new PropertyDescriptor.Builder()
        .name("Clone initialization flowfile")
        .description("Indicates whether the feed initialization flow will use a flowfile that is a clone of the input flowfile, i.e. includes all content.")
        .required(false)
        .allowableValues(CommonProperties.BOOLEANS)
        .defaultValue("true")
        .expressionLanguageSupported(true)
        .build();
    
    protected static final PropertyDescriptor MAX_FLOW_FILES_COUNT = new PropertyDescriptor.Builder()
        .name("Max Flow File Count")
        .description("The maximum number of flow files to process at one time")
        .required(false)
        .defaultValue("500")
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    
    protected static final PropertyDescriptor MAX_FLOW_FILES_SIZE = new PropertyDescriptor.Builder()
        .name("Max Flow Files size")
        .description("The maximum accumulated flow file sizes (in kilobytes) to process at one time")
        .required(false)
        .defaultValue("1000")
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    Relationship REL_INITIALIZE = new Relationship.Builder()
        .name("Initialize")
        .description("Begin initialization")
        .build();

    Relationship REL_REINITIALIZE = new Relationship.Builder()
        .name("Re-Initialize")
        .description("Begin re-initialization")
        .autoTerminateDefault(true)
        .build();

    private Map<String, AtomicInteger> retryCounts = Collections.synchronizedMap(new HashMap<>());

    @OnScheduled
    public void scheduled(ProcessContext context) {
        super.scheduled(context);
        this.retryCounts.clear();
    }

    @Override
    protected PropertyDescriptor getSupportedDynamicPropertyDescriptor(String propertyDescriptorName) {
        return new PropertyDescriptor.Builder()
            .name(propertyDescriptorName)
            .required(false)
            .addValidator(StandardValidators.createAttributeExpressionLanguageValidator(AttributeExpression.ResultType.STRING, true))
            .addValidator(StandardValidators.ATTRIBUTE_KEY_PROPERTY_NAME_VALIDATOR)
            .expressionLanguageSupported(true)
            .dynamic(true)
            .build();
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        int maxFlowFilesCount = context.getProperty(MAX_FLOW_FILES_COUNT).evaluateAttributeExpressions().asInteger();
        int maxFlowFilesSize = context.getProperty(MAX_FLOW_FILES_SIZE).evaluateAttributeExpressions().asInteger();
        Map<String, List<FlowFile>> batches = getFlowFileBatches(context, session, maxFlowFilesCount, maxFlowFilesSize);

        for (Entry<String, List<FlowFile>> entry : batches.entrySet()) {
            processFlowFiles(context, session, entry.getKey(), entry.getValue());
        }
   }

    private Map<String, List<FlowFile>> getFlowFileBatches(ProcessContext context, ProcessSession session, int maxFlowFilesCount, int maxFlowFilesSize) {
        Map<String, List<FlowFile>> map = new HashMap<>();
        List<FlowFile> allFiles = session.get(FlowFileFilters.newSizeBasedFilter(maxFlowFilesSize, DataUnit.KB, maxFlowFilesCount));
        
        allFiles.forEach(ff -> {
            FlowFile inputFF = ensureFeedId(context, session, ff);
            List<FlowFile> batch = map.computeIfAbsent(getFeedId(context, inputFF), k -> new ArrayList<>());
            batch.add(inputFF);
        });
        
        return map;
    }
    
    private void processFlowFiles(ProcessContext context, ProcessSession session, String feedId, List<FlowFile> batch) {
        if (batch != null) {
            InitializationStatus status = getMetadataRecorder().getInitializationStatus(feedId)
                .orElse(new InitializationStatus(State.PENDING));

            switch (status.getState()) {
                case PENDING:
                    pending(context, session, feedId, batch);
                    break;
                case IN_PROGRESS:
                    inProgress(context, session, feedId, batch);
                    break;
                case FAILED:
                    failed(context, session, feedId, batch, status.getTimestamp(), false);
                    break;
                case REINITIALIZE:
                    reinitialize(context, session, feedId, batch);
                    break;
                case REINITIALIZE_FAILED:
                    reinitializeFailed(context, session, feedId, batch, status.getTimestamp());
                    break;
                case SUCCESS:
                    success(context, session, feedId, batch);
            }
        }
    }

    @Override
    protected void addProperties(List<PropertyDescriptor> list) {
        super.addProperties(list);
        list.add(FAILURE_STRATEGY);
        list.add(RETRY_DELAY);
        list.add(MAX_INIT_ATTEMPTS);
        list.add(CLONE_INIT_FLOWFILE);
        list.add(MAX_FLOW_FILES_COUNT);
        list.add(MAX_FLOW_FILES_SIZE);
    }

    @Override
    protected void addRelationships(Set<Relationship> set) {
        super.addRelationships(set);
        set.add(CommonProperties.REL_SUCCESS);
        set.add(CommonProperties.REL_FAILURE);
        set.add(REL_INITIALIZE);
        set.add(REL_REINITIALIZE);
    }

    private void pending(ProcessContext context, ProcessSession session, String feedId, List<FlowFile> batch) {
        beginInitialization(context, session, feedId, batch, false);
        requeueFlowFiles(session, batch);
    }

    private void inProgress(ProcessContext context, ProcessSession session, String feedId, List<FlowFile> batch) {
        requeueFlowFiles(session, batch);
    }

    private void failed(ProcessContext context, ProcessSession session, String feedId, List<FlowFile> batch, DateTime failTime, boolean reinitializing) {
        String strategy = context.getProperty(FAILURE_STRATEGY).getValue();
        FlowFile inputFF = batch.stream().findFirst().get();  // batch size will always be > 0

        if (strategy.equals("RETRY")) {
            int delay = context.getProperty(RETRY_DELAY).evaluateAttributeExpressions(inputFF).asInteger();
            int max = context.getProperty(MAX_INIT_ATTEMPTS).evaluateAttributeExpressions(inputFF).asInteger();
            AtomicInteger count = getRetryCount(context, inputFF);

            if (count.getAndIncrement() >= max) {
                count.set(max);
                session.transfer(inputFF, CommonProperties.REL_FAILURE);
            } else if (failTime.plusSeconds(delay).isBefore(DateTime.now(DateTimeZone.UTC))) {
                beginInitialization(context, session, feedId, batch, reinitializing);
                requeueFlowFiles(session, batch);
            } else {
                session.transfer(inputFF, CommonProperties.REL_FAILURE);
            }
        } else {
            session.transfer(inputFF, CommonProperties.REL_FAILURE);
        }
    }

    private void reinitialize(ProcessContext context, ProcessSession session, String feedId, List<FlowFile> batch) {
        beginInitialization(context, session, feedId, batch, true);
        requeueFlowFiles(session, batch);
    }

    private void reinitializeFailed(ProcessContext context, ProcessSession session, String feedId, List<FlowFile> batch, DateTime failTime) {
        failed(context, session, feedId, batch, failTime, true);
    }

    private void success(ProcessContext context, ProcessSession session, String feedId, List<FlowFile> batch) {
        session.transfer(batch, CommonProperties.REL_SUCCESS);
    }

    private void beginInitialization(ProcessContext context, ProcessSession session, String feedId, List<FlowFile> batch, boolean reinitializing) {
        getMetadataRecorder().startFeedInitialization(feedId);
        FlowFile inputFF = batch.stream().findFirst().get();  // batch size will always be > 0
        FlowFile initFF;
        Relationship initRelationship;

        if (context.getProperty(CLONE_INIT_FLOWFILE).evaluateAttributeExpressions(inputFF).asBoolean()) {
            initFF = session.clone(inputFF);
        } else {
            initFF = session.create(inputFF);
        }

        if (reinitializing) {
            boolean useReinit = context.hasConnection(REL_REINITIALIZE);
            initRelationship = useReinit ? REL_REINITIALIZE : REL_INITIALIZE;
        } else {
            initRelationship = REL_INITIALIZE;
        }

        initFF = session.putAttribute(initFF, REINITIALIZING_FLAG, Boolean.valueOf(reinitializing).toString());
        session.transfer(initFF, initRelationship);
    }

    private void requeueFlowFiles(ProcessSession session, List<FlowFile> batch) {
        List<FlowFile> penalizedBatch = batch.stream()
                .map(inputFF -> session.penalize(inputFF))
                .collect(Collectors.toList());
        session.transfer(penalizedBatch);
    }

    private AtomicInteger getRetryCount(ProcessContext context, FlowFile inputFF) {
        return this.retryCounts.computeIfAbsent(getFeedId(context, inputFF), k -> new AtomicInteger(0));
    }

}
