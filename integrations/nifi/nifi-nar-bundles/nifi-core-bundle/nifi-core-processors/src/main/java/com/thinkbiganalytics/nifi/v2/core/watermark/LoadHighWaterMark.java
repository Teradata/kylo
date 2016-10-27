/**
 *
 */
package com.thinkbiganalytics.nifi.v2.core.watermark;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;
import com.thinkbiganalytics.nifi.core.api.metadata.WaterMarkActiveException;
import com.thinkbiganalytics.nifi.v2.common.CommonProperties;

/**
 * Loads a high-water mark and yields any processing if the water mark has not been released yet.
 *
 * @author Sean Felten
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"load", "high-water", "mark", "thinkbig"})
@CapabilityDescription("Loads and makes active a watermark associated with a feed.")
public class LoadHighWaterMark extends HighWaterMarkProcessor {

    protected static final AllowableValue[] ACTIVE_STRATEGY_VALUES = new AllowableValue[] {
                             new AllowableValue("YIELD", "Yield", "Yield processing so that another attempt to obtain the high-water mark can be made later"),
                             new AllowableValue("ROUTE", "Route", "Route immediately to the \"active\" relationship")
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

    public static final Relationship ACTIVE_FAILURE = new Relationship.Builder()
                    .name("activeFailure")
                    .description("The water mark is actively being processed and has not yet been committed or rejected")
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
        if (outputFF == null && ! context.hasNonLoopConnection()) {
            outputFF = session.create();
            createdFlowfile = true;
        }

        if (outputFF != null) {
            outputFF = initialize(context, session, outputFF);

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
                    getLog().error("Failed to load the current high-water mark: {} for feed {}", new Object[] {waterMark, feedId}, e);
                    session.transfer(outputFF, CommonProperties.REL_FAILURE);
                }

                this.yieldCount.set(0);
                session.transfer(outputFF, CommonProperties.REL_SUCCESS);
            } catch (WaterMarkActiveException e) {
                PropertyValue value = context.getProperty(MAX_YIELD_COUNT);
                int maxCount = value.isSet() ? value.asInteger() : Integer.MAX_VALUE - 1;
                int count = this.yieldCount.incrementAndGet();

                if (maxCount > 0 && count > maxCount) {
                    getLog().debug("Water mark {} is active - routing to \"activeFailure\"", new Object[] { waterMark });
                    session.transfer(outputFF, ACTIVE_FAILURE);
                } else {
                    getLog().debug("Yielding because water mark {} is active - attempt {} of {}", new Object[] { waterMark, count, maxCount });

                    // Remove the flow file before yielding if we create the flow file
                    if (createdFlowfile) {
                        session.remove(outputFF);
                        context.yield();
                    } else {
                        session.penalize(outputFF);
                        session.transfer(outputFF);
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
