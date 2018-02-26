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
import com.thinkbiganalytics.nifi.v2.core.savepoint.InvalidLockException;
import com.thinkbiganalytics.nifi.v2.core.savepoint.InvalidSetpointException;
import com.thinkbiganalytics.nifi.v2.core.savepoint.Lock;
import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointController;
import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.expression.AttributeExpression;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventDriven
@SupportsBatching
@Tags({"savepoint", "thinkbig", "kylo"})
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@CapabilityDescription("Provides a mechanism to trigger upstream savepoints to either retry or release  a flowfile. A retry behavior signal will retry up until the max retries using the penalty"
                       + " period in the processor for the retry interval.")
@SeeAlso(classNames = {"com.thinkbiganalytics.nifi.v2.savepoint.TriggerSetpoint"})
public class TriggerSavepoint extends AbstractProcessor {

    /**
     * Marker to indicate flowfile has been seen before as part of a retry process. A flowfile marked for retry will be penalized once based on the penalize duration.
     */
    public static final String SAVEPOINT_RETRY_MARKER = "savepoint.retry.marker";

    /**
     * Counter for failures to process the flowfile. Failures will be penalized
     */
    public static final String SAVEPOINT_TRIGGER_FAILURE_COUNT = "savepoint.trigger.retry.count";


    /**
     * Flowfile attr indicating max retries exceeded
     */
    public static final String SAVE_POINT_MAX_RETRIES_EXCEEDED = "savepoint.max.retries.exceeded";


    /**
     * Status description
     */
    public static final String SAVE_POINT_BEHAVIOR_STATUS_DESC = "savepoint.behavior.status.desc";


    public static final String RETRY = SavepointProvenanceProperties.TRIGGER_SAVE_POINT_STATE.RETRY.name();
    public static final String RELEASE = SavepointProvenanceProperties.TRIGGER_SAVE_POINT_STATE.RELEASE.name();
    public static final String FAIL = SavepointProvenanceProperties.TRIGGER_SAVE_POINT_STATE.FAIL.name();

    // Number of times a flowfile can fail to process before routing to failure
    public static final int MAX_FAILURES_ALLOWED = 3;

    public static final PropertyDescriptor SAVEPOINT_SERVICE = new PropertyDescriptor.Builder()
        .name("savepoint-service")
        .displayName("Savepoint service")
        .description("The Controller Service used to manage retry and release signals for savepoints.")
        .required(true)
        .identifiesControllerService(SavepointController.class)
        .build();

    // Selects the FlowFile attribute or expression, whose value is used as cache key
    public static final PropertyDescriptor SAVEPOINT_ID = new PropertyDescriptor.Builder()
        .name("savepoint-id")
        .displayName("Savepoint Id")
        .description("A value, or the results of an Attribute Expression Language statement, which will " +
                     "be evaluated against a FlowFile in order to determine the savepoint key")
        .required(true)
        .addValidator(StandardValidators.createAttributeExpressionLanguageValidator(AttributeExpression.ResultType.STRING, true))
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor BEHAVIOR = new PropertyDescriptor.Builder()
        .name("behavior")
        .displayName("Behavior")
        .description("The behavior to apply to the setpoint")
        .required(true)
        .allowableValues(new String[]{RETRY, RELEASE, FAIL})
        .expressionLanguageSupported(false)
        .build();

    public static final PropertyDescriptor MAX_RETRIES = new PropertyDescriptor.Builder()
        .name("max-retries")
        .displayName("Max Retries")
        .description("Maximum number of retries before flowfile is routed to failure.")
        .required(false)
        .defaultValue("10")
        .addValidator(StandardValidators.INTEGER_VALIDATOR)
        .expressionLanguageSupported(false)
        .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("A successful operation will result in FlowFile on this relationship")
        .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("FlowFiles will be routed to this relationship if the route_to_failure property is true or an error occurs")
        .build();

    public static final Relationship REL_MAX_RETRIES_EXCEEDED = new Relationship.Builder()
        .name("max retries")
        .description("FlowFiles that exceeded maximum retries are routed to this relationship")
        .build();


    public static final Relationship REL_SELF = Relationship.SELF;


    private final Set<Relationship> relationships;

    public TriggerSavepoint() {
        final Set<Relationship> rels = new HashSet<>();
        rels.add(REL_SUCCESS);
        rels.add(REL_FAILURE);
        rels.add(REL_MAX_RETRIES_EXCEEDED);
        relationships = Collections.unmodifiableSet(rels);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final List<PropertyDescriptor> descriptors = new ArrayList<>();

        descriptors.add(SAVEPOINT_SERVICE);
        descriptors.add(SAVEPOINT_ID);
        descriptors.add(BEHAVIOR);
        descriptors.add(MAX_RETRIES);

        return descriptors;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    private Relationship route(boolean routeToFailure) {
        return (routeToFailure ? REL_FAILURE : REL_SUCCESS);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        // Fetch the controller
        final SavepointController controller = context.getProperty(SAVEPOINT_SERVICE).asControllerService(SavepointController.class);
        final SavepointProvider provider = controller.getProvider();

        final ComponentLog logger = getLogger();

        final PropertyValue pvSavepointId = context.getProperty(SAVEPOINT_ID);
        final PropertyValue pvBehavior = context.getProperty(BEHAVIOR);
        final PropertyValue pvMaxRetries = context.getProperty(MAX_RETRIES);

        // We do processing on each flowfile here

        String behavior = pvBehavior.getValue();
        if (!FAIL.equals(behavior)) {

            final String savepointIdStr = pvSavepointId.evaluateAttributeExpressions(flowFile).getValue();
            Lock lock = null;
            try {
                lock = provider.lock(savepointIdStr);
                if (lock != null) {

                    if (RELEASE.equals(behavior)) {
                        provider.release(savepointIdStr, lock, true);
                        flowFile = session.putAttribute(flowFile, SavepointProvenanceProperties.SAVE_POINT_BEHAVIOR_STATUS, behavior);
                        session.transfer(flowFile, REL_SUCCESS);

                    } else if (RETRY.equals(behavior)) {

                        // Check the retry count from the SetSavepoint
                        String sRetryCount = flowFile.getAttribute(SetSavepoint.SAVEPOINT_RETRY_COUNT);
                        int retryCount = 0;
                        try {
                            if (sRetryCount != null) {
                                retryCount = Integer.parseInt(sRetryCount);
                            }
                        } catch (NumberFormatException nfe) {
                            logger.warn("{} has an invalid value '{}' on FlowFile {}", new Object[]{SetSavepoint.SAVEPOINT_RETRY_COUNT, sRetryCount, flowFile});
                        }

                        // Check retries
                        if (retryCount > pvMaxRetries.asInteger()) {
                            flowFile = session.putAttribute(flowFile, TriggerSavepoint.SAVE_POINT_MAX_RETRIES_EXCEEDED, sRetryCount);
                            session.transfer(flowFile, REL_MAX_RETRIES_EXCEEDED);
                            return;
                        }

                        // Penalize the flowfile once before retry is processed
                        String sRetryMarker = flowFile.getAttribute(SAVEPOINT_RETRY_MARKER);
                        if (StringUtils.isEmpty(sRetryMarker)) {
                            flowFile = session.penalize(flowFile);
                            flowFile = session.putAttribute(flowFile, SAVEPOINT_RETRY_MARKER, "1");
                            session.transfer(flowFile, REL_SELF);
                            return;
                        }

                        provider.retry(savepointIdStr, lock);
                        session.transfer(flowFile, REL_SUCCESS);

                    }
                } else {
                    // Unable to obtain lock. Try again
                    session.transfer(flowFile, REL_SELF);
                }
            } catch (IOException | InvalidLockException | InvalidSetpointException e) {

                logger.info("Exception occurred for FlowFile {} exception {}", new Object[]{flowFile, e.getLocalizedMessage()}, e);

                // Check the retry count from the SetSavepoint
                String sTriggerFailureCount = flowFile.getAttribute(TriggerSavepoint.SAVEPOINT_TRIGGER_FAILURE_COUNT);
                int triggerFailureCount = 1;
                try {
                    triggerFailureCount = (sTriggerFailureCount == null ? 0 : Integer.parseInt(sTriggerFailureCount));
                    triggerFailureCount += 1;
                } catch (NumberFormatException nfe) {
                    logger.info("Invalid attribute {}", new Object[]{TriggerSavepoint.SAVEPOINT_TRIGGER_FAILURE_COUNT});
                }
                flowFile = session.putAttribute(flowFile, TriggerSavepoint.SAVEPOINT_TRIGGER_FAILURE_COUNT, String.valueOf(triggerFailureCount));

                if (triggerFailureCount > MAX_FAILURES_ALLOWED) {
                    logger.info("Maximum failures reached for sp {}, will route to fail.", new String[]{savepointIdStr});
                    flowFile = session.putAttribute(flowFile, SavepointProvenanceProperties.SAVE_POINT_BEHAVIOR_STATUS, FAIL);
                    flowFile = session.putAttribute(flowFile, TriggerSavepoint.SAVE_POINT_BEHAVIOR_STATUS_DESC, "Maximum failures at " + triggerFailureCount + " were reached.  Failing the flow");

                    //add in the trigger flow id so ops manager can get the key to retry if needed
                    String triggerFlowFile = flowFile.getAttribute(SavepointProvenanceProperties.PARENT_FLOWFILE_ID);
                    if(StringUtils.isNotBlank(triggerFlowFile)) {
                        flowFile = session.putAttribute(flowFile, SavepointProvenanceProperties.SAVE_POINT_TRIGGER_FLOWFILE, triggerFlowFile);
                    }
                    session.transfer(flowFile, REL_FAILURE);
                } else {
                    logger.info("Failed to process flowfile for savepoint {}", new String[]{savepointIdStr}, e);
                    flowFile = session.penalize(flowFile);
                    session.transfer(flowFile, REL_SELF);
                }

            } finally {
                if (lock != null) {
                    try {
                        provider.unlock(lock);
                    } catch (IOException e) {
                        logger.warn("Unable to unlock {}", new String[]{savepointIdStr});
                    }
                }
            }
        } else {
            // Route to failure
            flowFile = session.putAttribute(flowFile, SavepointProvenanceProperties.SAVE_POINT_BEHAVIOR_STATUS, behavior);
            String triggerFlowFile = flowFile.getAttribute(SavepointProvenanceProperties.PARENT_FLOWFILE_ID);
            if(StringUtils.isNotBlank(triggerFlowFile)) {
                flowFile = session.putAttribute(flowFile, SavepointProvenanceProperties.SAVE_POINT_TRIGGER_FLOWFILE, triggerFlowFile);
            }
            session.transfer(flowFile, REL_FAILURE);
        }
    }


}
