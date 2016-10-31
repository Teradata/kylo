/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.metadata.api.op.FeedDependencyDeltaResults;
import com.thinkbiganalytics.metadata.rest.model.Formatters;
import com.thinkbiganalytics.metadata.rest.model.event.FeedPreconditionTriggerEvent;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants;
import com.thinkbiganalytics.nifi.core.api.precondition.FeedPreconditionEventService;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionListener;
import com.thinkbiganalytics.nifi.v2.common.CommonProperties;
import com.thinkbiganalytics.util.ComponentAttributes;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants.OPERATON_START_PROP;
import static com.thinkbiganalytics.nifi.v2.common.CommonProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.common.CommonProperties.FEED_NAME;

/**
 * @author Sean Felten
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "trigger", "thinkbig"})
@CapabilityDescription("Triggers the execution of a feed whenever the conditions defined by its precondition have been met.  This process should be the first processor in a flow depends upon preconditions.")
public class TriggerFeed extends AbstractFeedProcessor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Queue<FeedPreconditionTriggerEvent> triggerEventQueue = new LinkedBlockingQueue<>();
    private transient PreconditionListener preconditionListener;
    private transient String feedId;

    public static final PropertyDescriptor PRECONDITION_SERVICE = new PropertyDescriptor.Builder()
            .name("Feed Precondition Event Service")
            .description("Service that manages preconditions which trigger feed execution")
            .required(true)
            .identifiesControllerService(FeedPreconditionEventService.class)
            .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("Success")
            .description("Relationship followed on successful precondition event.")
            .build();


    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);

        this.preconditionListener = createPreconditionListener();
    }

    @OnScheduled
    public void scheduled(ProcessContext context) {
        String category = context.getProperty(CommonProperties.FEED_CATEGORY).getValue();
        String feedName = context.getProperty(CommonProperties.FEED_NAME).getValue();

        try {
            this.feedId = getProviderService(context).getProvider().getFeedId(category, feedName);
        } catch (Exception e) {
            getLog().warn("Failure retrieving feed metadata" + category + "/" + feedName, e);
            // TODO Swallowing for now until metadata client is working again
        }

        registerPreconditonListener(context, category, feedName);
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = produceFlowFile(context, session);

        while (flowFile != null) {
            String feedName = context.getProperty(FEED_NAME).getValue();

            flowFile = session.putAttribute(flowFile, MetadataConstants.FEED_NAME_PROP, feedName);
            flowFile = session.putAttribute(flowFile, OPERATON_START_PROP, Formatters.print(new DateTime()));

            session.transfer(flowFile, SUCCESS);

            flowFile = produceFlowFile(context, session);
            if (flowFile == null) {
                context.yield();
            }
        }
    }

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(PRECONDITION_SERVICE);
        props.add(FEED_CATEGORY);
        props.add(FEED_NAME);
    }

    @Override
    protected void addRelationships(Set<Relationship> rels) {
        super.addRelationships(rels);
        rels.add(SUCCESS);
    }

    protected FeedPreconditionEventService getPreconditionService(ProcessContext context) {
        return context.getProperty(PRECONDITION_SERVICE).asControllerService(FeedPreconditionEventService.class);
    }

    protected FlowFile produceFlowFile(ProcessContext context, ProcessSession session) {
        FeedPreconditionTriggerEvent event = this.triggerEventQueue.poll();
        if (event != null) {
            return createFlowFile(context, session, event);
        } else {
            return session.get();
        }
    }

    private FlowFile createFlowFilex(ProcessContext context,
                                    ProcessSession session,
                                    FeedPreconditionTriggerEvent event) {
        FlowFile file = session.create();
        getLog().info("createFlowFile for Feed {}", new Object[]{this.feedId});
        if (this.feedId != null) {
            FeedDependencyDeltaResults props = getProviderService(context).getProvider().getFeedDependentResultDeltas(this.feedId);
            try {

                String value = MAPPER.writeValueAsString(props);
                //add the json as an attr value?
                //   file = session.putAttribute(file, ComponentAttributes.FEED_DEPENDENT_RESULT_DELTAS.key(), value);
                //write the json back to the flow file content
                file = session.write(file, new OutputStreamCallback() {
                    @Override
                    public void process(OutputStream outputStream) throws IOException {
                        outputStream.write(value.getBytes(StandardCharsets.UTF_8));
                    }
                });
            } catch (JsonProcessingException e) {
                getLog().warn("Failed to serialize feed dependency result deltas", e);
                // TODO Swallow the exception and produce the flow file anyway?
            }
        }

        return file;
    }


    private FlowFile createFlowFile(ProcessContext context,
                                    ProcessSession session,
                                    FeedPreconditionTriggerEvent event) {

        getLog().info("createFlowFile for Feed {}", new Object[]{this.feedId});
        FlowFile file = null;
        if (this.feedId != null) {
            FeedDependencyDeltaResults deltas = getProviderService(context).getProvider().getFeedDependentResultDeltas(this.feedId);
            if (deltas != null && deltas.getDependentFeedNames() != null && !deltas.getDependentFeedNames().isEmpty()) {
                file = session.create();

                try {

                    String value = MAPPER.writeValueAsString(deltas);
                    //add the json as an attr value?
                    file = session.putAttribute(file, ComponentAttributes.FEED_DEPENDENT_RESULT_DELTAS.key(), value);
                    //write the json back to the flow file content
                    file = session.write(file, new OutputStreamCallback() {
                        @Override
                        public void process(OutputStream outputStream) throws IOException {
                            outputStream.write(value.getBytes(StandardCharsets.UTF_8));
                        }
                    });
                } catch (JsonProcessingException e) {
                    getLog().warn("Failed to serialize feed dependency result deltas", e);
                    // TODO Swallow the exception and produce the flow file anyway?
                }
            }
        }
        if (file == null) {
            file = session.get();
        }

        return file;
    }

    private void registerPreconditonListener(ProcessContext context, String category, String feedName) {
        FeedPreconditionEventService precondService = getPreconditionService(context);

        precondService.addListener(category, feedName, preconditionListener);
    }

    private PreconditionListener createPreconditionListener() {
        PreconditionListener listener = new PreconditionListener() {
            @Override
            public void triggered(FeedPreconditionTriggerEvent event) {
                getLog().debug("Precondition event triggered: {}", new Object[]{event});

                TriggerFeed.this.triggerEventQueue.add(event);
            }
        };
        return listener;
    }

}
