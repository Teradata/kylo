package com.thinkbiganalytics.nifi.v2.savepoint;

/*-
 * #%L
 * kylo-nifi-core-processors
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

import com.thinkbiganalytics.nifi.savepoint.api.SavepointProvenanceProperties;
import com.thinkbiganalytics.nifi.v2.core.savepoint.CacheNotInitializedException;
import com.thinkbiganalytics.nifi.v2.core.savepoint.InvalidLockException;
import com.thinkbiganalytics.nifi.v2.core.savepoint.InvalidSetpointException;
import com.thinkbiganalytics.nifi.v2.core.savepoint.Lock;
import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointController;
import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointEntry;
import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.expression.AttributeExpression.ResultType;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.FlowFileFilter;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@EventDriven
@SupportsBatching
@Tags({"savepoint", "thinkbig", "kylo"})
@InputRequirement(Requirement.INPUT_REQUIRED)
@CapabilityDescription("Provides a mechanism for creating savepoints for a flowfile in a particular state that can be retried if a downstream sequence fails. Maintains a queue of FlowFiles until a "
                       + "retry or release signal is provided"
                       + "from the controller. "
                       + "When a new flowfile arrives or a retry signal is received, a waiting FlowFile is cloned to the 'try' relationship, "
                       + "savepoint.retry.count attribute is incremented on the FlowFile and the original FlowFile also remains in the 'wait' "
                       + "relationship. The release signal causes the FlowFile to be removed. Waiting FlowFiles will be routed to 'expired' "
                       + "if they exceed the Expiration Duration. "
)
@WritesAttributes({
                      @WritesAttribute(attribute = "savepoint.start.timestamp", description = "All FlowFiles will have an attribute 'setpoint.start.timestamp', which sets the "
                                                                                              + "initial epoch timestamp when the file first entered this processor.  This is used to determine the expiration time of the FlowFile."),
                      @WritesAttribute(attribute = "savepoint.retry.count", description = "Incremented each time a retry is invoked on the flowfile.")
                  })
@SeeAlso(classNames = {"com.thinkbiganalytics.nifi.v2.savepoint.TriggerSetpoint"})
public class SetSavepoint extends AbstractProcessor {

    public static final String SAVEPOINT_RETRY_COUNT = "savepoint.retry.count";
    public static final String SAVEPOINT_START_TIMESTAMP = "savepoint.start.timestamp";

    public static final String SAVEPOINT_PROCESSOR_ID = "savepoint.processor";

    public static final String SAVEPOINT_EXCEPTION = "savepoint.exception";


    // Identifies the savepoint service
    public static final PropertyDescriptor SAVEPOINT_SERVICE = new PropertyDescriptor.Builder()
        .name("savepoint-service")
        .displayName("Savepoint service")
        .description("The Controller Service used to manage retry and release signals for savepoints.")
        .required(true)
        .identifiesControllerService(SavepointController.class)
        .build();

    // Selects the FlowFile attribute or expression, whose value is used as savepoint key
    public static final PropertyDescriptor SAVEPOINT_ID = new PropertyDescriptor.Builder()
        .name("savepoint-id")
        .displayName("Savepoint Id")
        .description("A value, or the results of an Attribute Expression Language statement, which will " +
                     "be evaluated against a FlowFile in order to determine the savepoint key")
        .required(true)
        .addValidator(StandardValidators.createAttributeExpressionLanguageValidator(ResultType.STRING, true))
        .expressionLanguageSupported(true)
        .build();

    // Sets the duration before expiring the savepoint all together.
    public static final PropertyDescriptor EXPIRATION_DURATION = new PropertyDescriptor.Builder()
        .name("expiration-duration")
        .displayName("Expiration Duration")
        .description("Indicates the duration after which waiting FlowFiles will be routed to the 'expired' relationship")
        .required(true)
        .defaultValue("72 hours")
        .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
        .expressionLanguageSupported(false)
        .build();


    public static final Relationship REL_TRY = new Relationship.Builder()
        .name("try")
        .description("A FlowFile will be cloned upon new entry or receiving a retry signal from the controller and routed to this relationship")
        .build();

    public static final Relationship REL_RELEASE_SUCCESS = new Relationship.Builder()
        .name("release-success")
        .description("A FlowFile with a release signal from controller will be routed to this relationship")
        .build();

    public static final Relationship REL_RELEASE_FAILURE = new Relationship.Builder()
        .name("release-failure")
        .description("A FlowFile with a release signal from controller will be routed to this relationship")
        .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("FlowFiles will be routed to this relationship if the controller cannot be reached or Savepoint Identifier evaluates to null or empty")
        .build();

    public static final Relationship REL_SELF = Relationship.SELF;

    public static final Relationship REL_EXPIRED = new Relationship.Builder()
        .name("expired")
        .description("A FlowFile that has exceeded the configured expiration Duration will be routed to this relationship")
        .build();

    private final Set<Relationship> relationships;

    public SetSavepoint() {
        final Set<Relationship> rels = new HashSet<>();
        rels.add(REL_TRY);
        rels.add(REL_RELEASE_SUCCESS);
        rels.add(REL_RELEASE_FAILURE);
        rels.add(REL_EXPIRED);
        rels.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(rels);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(SAVEPOINT_ID);
        descriptors.add(EXPIRATION_DURATION);
        descriptors.add(SAVEPOINT_SERVICE);

        return descriptors;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }


    /**
     * Check to see if a flowfile in queue has an expired savepoint
     *
     * @param flowFile           the flowfile
     * @param expirationDuration the expire duration
     * @return true if expired, false if not
     */
    private boolean isExpired(FlowFile flowFile, long expirationDuration) {
        boolean expired = false;
        // Set wait start timestamp if it's not set yet
        String waitStartTimestamp = flowFile.getAttribute(SAVEPOINT_START_TIMESTAMP);
        if (waitStartTimestamp != null) {
            long lWaitStartTimestamp = 0L;
            try {
                lWaitStartTimestamp = Long.parseLong(waitStartTimestamp);
            } catch (NumberFormatException nfe) {
                expired = false;
            }

            // check for expiration
            long now = System.currentTimeMillis();
            if (now > (lWaitStartTimestamp + expirationDuration)) {
                expired = true;
            }
        }
        return expired;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        final SavepointController controller = context.getProperty(SAVEPOINT_SERVICE).asControllerService(SavepointController.class);
        final SavepointProvider provider = controller.getProvider();

        final PropertyValue pvSavepointId = context.getProperty(SAVEPOINT_ID);
        final String processorId = getIdentifier();

        FlowFile flowFile = null;
        long start = System.currentTimeMillis();
        Optional<FlowFile> nextFlowfile = getNextFlowFile(context, session,controller, provider, pvSavepointId);

        long stop = System.currentTimeMillis();
        if (!nextFlowfile.isPresent()) {
            return;
        } else {
            flowFile = nextFlowfile.get();
        }
        getLogger().info("Time to iterate over {} flow files: {} ms, {} ", new Object[]{session.getQueueSize(), (stop - start), nextFlowfile.isPresent() ? nextFlowfile.get() : " Nothing found "});

        final ComponentLog logger = getLogger();

        // We do processing on each flowfile here
        final String savepointIdStr = pvSavepointId.evaluateAttributeExpressions(flowFile).getValue();

        final String flowfileId = flowFile.getAttribute(CoreAttributes.UUID.key());
        Lock lock = null;
        try {
            lock = provider.lock(savepointIdStr);
            if (lock != null) {
                SavepointEntry entry = provider.lookupEntry(savepointIdStr);

                if (isExpired(context, session, provider, flowFile, savepointIdStr, lock)) {
                    return;
                }
                String waitStartTimestamp;
                //add the processor id for the current savepoint
                //this will be used to check on the next save point if the flow file should be examined and processed.
                flowFile = session.putAttribute(flowFile, SAVEPOINT_PROCESSOR_ID, getIdentifier());

                if (entry == null || entry.getState(processorId) == null) {
                    // Register new
                    provider.register(savepointIdStr, processorId, flowfileId, lock);
                    flowFile =  tryFlowFile(session, flowFile, "-1");

                    //add in timestamps
                    // Set wait start timestamp if it's not set yet
                    waitStartTimestamp = flowFile.getAttribute(SAVEPOINT_START_TIMESTAMP);
                    if (waitStartTimestamp == null) {
                        waitStartTimestamp = String.valueOf(System.currentTimeMillis());
                        flowFile = session.putAttribute(flowFile, SAVEPOINT_START_TIMESTAMP, waitStartTimestamp);
                    }
                    session.transfer(flowFile);

                } else {
                    SavepointEntry.SavePointState state = entry.getState(processorId);
                    switch (state) {
                        case RELEASE_SUCCESS:
                            provider.commitRelease(savepointIdStr, processorId, lock);
                            //add provenance to indicate success
                            flowFile = session.putAttribute(flowFile, SavepointProvenanceProperties.RELEASE_STATUS_KEY, SavepointProvenanceProperties.RELEASE_STATUS.SUCCESS.name());
                            session.transfer(flowFile, REL_RELEASE_SUCCESS);
                            break;
                        case RELEASE_FAILURE:
                            provider.commitRelease(savepointIdStr, processorId, lock);
                            //add provenance to indicate failure
                            flowFile = session.putAttribute(flowFile, SavepointProvenanceProperties.RELEASE_STATUS_KEY, SavepointProvenanceProperties.RELEASE_STATUS.FAILURE.name());
                            session.transfer(flowFile, REL_RELEASE_FAILURE);
                            break;
                        case RETRY:
                            String retryCount = flowFile.getAttribute(SAVEPOINT_RETRY_COUNT);
                            if (retryCount == null) {
                                retryCount = "0";
                            }
                            provider.commitRetry(savepointIdStr, processorId, lock);
                           flowFile = tryFlowFile(session, flowFile, retryCount);
                           session.transfer(flowFile);
                            break;
                        case WAIT:
                            session.transfer(flowFile, REL_SELF);
                            break;
                        default:
                            logger.warn("Unexpected savepoint state.");
                            session.transfer(flowFile, REL_FAILURE);

                    }
                }

            } else {
                // Lock busy so try again later
                //add it back to cache
                 controller.putFlowfileBack(processorId, flowfileId);
                logger.info("Unable to obtain lock.  It is already locked by another process.  Adding back to queue {} ",new Object[] {flowfileId});

                session.transfer(flowFile, REL_SELF);

            }
        } catch (IOException | InvalidLockException | InvalidSetpointException e) {
            logger.warn("Failed to process flowfile {} for savepoint {}", new String[]{flowfileId,savepointIdStr}, e);
            flowFile = session.putAttribute(flowFile, SAVEPOINT_EXCEPTION, "Failed to process flowfile "+flowfileId+" for savepoint " + savepointIdStr + ". " + e.getMessage());
            session.transfer(flowFile, REL_FAILURE);
        } finally {
            if (lock != null) {
                try {
                    provider.unlock(lock);
                } catch (IOException e) {
                    logger.warn("Unable to unlock {}", new String[]{savepointIdStr});
                }
            }
        }

    }

    private boolean isExpired(ProcessContext context, ProcessSession session, SavepointProvider provider, FlowFile flowFile, String savepointIdStr,
                                 Lock lock) throws InvalidLockException, InvalidSetpointException {

        String waitStartTimestamp = flowFile.getAttribute(SAVEPOINT_START_TIMESTAMP);
        long lWaitStartTimestamp = 0L;
        if(StringUtils.isNotBlank(waitStartTimestamp)) {
            try {
                 lWaitStartTimestamp = Long.parseLong(waitStartTimestamp);
            }catch (NumberFormatException e){
                getLogger().warn("{} has an invalid value '{}' on FlowFile {}. Time will be reset.", new Object[]{SAVEPOINT_START_TIMESTAMP, waitStartTimestamp, flowFile});
                flowFile = session.putAttribute(flowFile, SAVEPOINT_START_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                session.transfer(flowFile, REL_SELF);
                return true;
            }
            // check for expiration
            long expirationDuration = context.getProperty(EXPIRATION_DURATION)
                .asTimePeriod(TimeUnit.MILLISECONDS);
            long now = System.currentTimeMillis();
            if (now > (lWaitStartTimestamp + expirationDuration)) {
                getLogger().info("FlowFile {} expired after {}ms", new Object[]{flowFile, (now - lWaitStartTimestamp)});
                provider.commitRelease(savepointIdStr, getIdentifier(), lock);
                session.transfer(flowFile, REL_EXPIRED);
                return true;
            }
        }
        return false;
    }


    private class FindFirstFlowFileFilter implements FlowFileFilter {

        private Optional<String> nextFlowFile;

        private long expirationDuration;

        private SavepointController controller;

        public FindFirstFlowFileFilter(Optional<String> nextFlowFile,long expirationDuration,SavepointController controller){
            this.nextFlowFile = nextFlowFile;
            this.expirationDuration = expirationDuration;
            this.controller = controller;
        }

        private boolean isNew(FlowFile flowFile){
            String savepointProcessor = flowFile.getAttribute(SAVEPOINT_PROCESSOR_ID);
            return StringUtils.isBlank(savepointProcessor) || !getIdentifier().equalsIgnoreCase(savepointProcessor);
        }

        @Override
        public FlowFileFilterResult filter(FlowFile flowFile) {
            if(!nextFlowFile.isPresent()){
                //check to see if its new or expired
                if(isExpired(flowFile, expirationDuration) || isNew(flowFile)){
                    return FlowFileFilterResult.ACCEPT_AND_TERMINATE;
                }
                return FlowFileFilter.FlowFileFilterResult.REJECT_AND_CONTINUE;
            }
            else if(nextFlowFile.get().equals(flowFile.getAttribute(CoreAttributes.UUID.key()))){
                return FlowFileFilter.FlowFileFilterResult.ACCEPT_AND_TERMINATE;
            }
            else {
                return FlowFileFilter.FlowFileFilterResult.REJECT_AND_CONTINUE;
            }


        }
    }

    private class CacheInitializingFilter implements FlowFileFilter {

        private PropertyValue pvSavepointId;

        private SavepointProvider provider;
        private SavepointController controller;

        Map<Long,SavepointEntry>savepointsToCache = new TreeMap<>();

        Map<Long,String>flowfilesToCache = new TreeMap<>();

        private long expirationDuration;

        public CacheInitializingFilter(PropertyValue pvSavepointId,SavepointController controller, SavepointProvider provider, long expirationDuration) {
            this.pvSavepointId = pvSavepointId;
            this.provider = provider;
            this.controller = controller;
            this.expirationDuration = expirationDuration;
        }

        private SavepointEntry toSavepoint(String processorId, String flowfileId){
            SavepointEntry savepointEntry = new SavepointEntry();
            savepointEntry.register(processorId,flowfileId);
            savepointEntry.retry();
            return  savepointEntry;
        }

        public FlowFileFilterResult filter(FlowFile f) {
            final String savepointIdStr = pvSavepointId.evaluateAttributeExpressions(f).getValue();
            SavepointEntry entry = provider.lookupEntry(savepointIdStr);
            if (entry == null || entry.getState(getIdentifier()) == null){
                flowfilesToCache.put(f.getLastQueueDate(),f.getAttribute(CoreAttributes.UUID.key()));
            } else if(isExpired(f, expirationDuration)) {
                savepointsToCache.put(f.getLastQueueDate(),entry);
            } else if (SavepointEntry.SavePointState.WAIT != entry.getState(getIdentifier())) {
                savepointsToCache.put(f.getLastQueueDate(),entry);
            }
            return FlowFileFilter.FlowFileFilterResult.REJECT_AND_CONTINUE;
        }

        Optional<FlowFile> initializeAndGetNextFlowfile(ProcessSession session){
            flowfilesToCache = new TreeMap<>();
            //initialize the map to cache
            session.get(this);
            //cache it
            Optional<String> nextFlowFile = controller.initializeAndGetNextFlowFile(getIdentifier(),flowfilesToCache.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()),savepointsToCache.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
            //refetch
            return session.get(new FindFirstFlowFileFilter(nextFlowFile,expirationDuration,controller)).stream().findFirst();
        }

    }

    /**
     * Return the next available flow file in the queue that is not in a waiting state.
     *
     * @param session       the process session
     * @param provider      the save point provider
     * @param pvSavepointId the savepoint id
     * @return the first flowfile not in a waiting savepoint state
     */
    private Optional<FlowFile> getNextFlowFile(ProcessContext context, ProcessSession session,SavepointController controller, SavepointProvider provider, PropertyValue pvSavepointId) {

        long expirationDuration = context.getProperty(EXPIRATION_DURATION)
            .asTimePeriod(TimeUnit.MILLISECONDS);

        FlowFileFilter flowFileFilter = null;
        try {
            Optional<String> nextFlowFile = controller.getNextFlowFile(getIdentifier());
            flowFileFilter = new FindFirstFlowFileFilter(nextFlowFile, expirationDuration,controller);
            return session.get(flowFileFilter).stream().findFirst();
        } catch (CacheNotInitializedException e) {
            CacheInitializingFilter filter = new CacheInitializingFilter(pvSavepointId, controller, provider, expirationDuration);
            return filter.initializeAndGetNextFlowfile(session);
        }

    }


    private Optional<FlowFile> getNextFlowFilex(ProcessContext context, ProcessSession session, SavepointProvider provider, PropertyValue pvSavepointId) {
        long expirationDuration = context.getProperty(EXPIRATION_DURATION)
            .asTimePeriod(TimeUnit.MILLISECONDS);

        List<FlowFile> match = new ArrayList<>();
        List<FlowFile> noMatch = new LinkedList<>();

        session.get(session.getQueueSize().getObjectCount()).stream()
            .sorted(Comparator.comparing(FlowFile::getLastQueueDate)
                        .reversed()).forEach(f -> {
            boolean isMatch = false;
            if (match.isEmpty()) {
                final String savepointIdStr = pvSavepointId.evaluateAttributeExpressions(f).getValue();
                String processorId = getIdentifier();
                SavepointEntry entry = provider.lookupEntry(savepointIdStr);

                if (entry == null || entry.getState(processorId) == null || isExpired(f, expirationDuration)) {
                    isMatch = true;
                } else if (SavepointEntry.SavePointState.WAIT != entry.getState(processorId)) {
                    isMatch = true;
                }
                //add it
                if (isMatch) {
                    match.add(f);
                }
                else {
                    noMatch.add(f);
                }
            } else {
                noMatch.add(f);
            }
        });
        //clear those that failed
        session.transfer(noMatch);

        return match.isEmpty() ? Optional.empty() : Optional.of(match.get(0));

    }

    /**
     * Try or retry a flowfile
     */
    private FlowFile tryFlowFile(final ProcessSession session, final FlowFile flowFile, String retryCount) {
        FlowFile flowFileModified = session.putAttribute(flowFile, SAVEPOINT_RETRY_COUNT, StringUtils.defaultString(String.valueOf(Integer.parseInt(retryCount) + 1), "1"));
        FlowFile clonedFlowFile = session.clone(flowFileModified);

        flowFileModified = session.putAttribute(flowFileModified, SavepointProvenanceProperties.CLONE_FLOWFILE_ID, clonedFlowFile.getAttribute(CoreAttributes.UUID.key()));
        clonedFlowFile = session.putAttribute(clonedFlowFile, SavepointProvenanceProperties.PARENT_FLOWFILE_ID, flowFileModified.getAttribute(CoreAttributes.UUID.key()));

        session.transfer(clonedFlowFile, REL_TRY);
        return flowFileModified;
    }
}
