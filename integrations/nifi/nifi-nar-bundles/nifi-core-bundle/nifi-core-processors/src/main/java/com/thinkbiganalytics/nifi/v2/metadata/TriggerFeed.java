/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.metadata.rest.model.Formatters;
import com.thinkbiganalytics.metadata.rest.model.event.FeedPreconditionTriggerEvent;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants;
import com.thinkbiganalytics.nifi.core.api.precondition.FeedPreconditionEventService;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionListener;
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
import org.apache.nifi.processor.util.StandardValidators;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants.OPERATON_START_PROP;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.FEED_CATEGORY;
import static com.thinkbiganalytics.nifi.v2.ingest.ComponentProperties.FEED_NAME;

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

    PropertyDescriptor META_FEED_CATEGORY = new PropertyDescriptor.Builder()
            .name("System feed category")
            .description("The category name of this feed.  The default is to have this name automatically set when the feed is created.  "
                            + "Normally you do not need to change the default value.")
            .required(true)
            .defaultValue("${metadata.category.systemName}")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    PropertyDescriptor META_FEED_NAME = new PropertyDescriptor.Builder()
            .name("System feed name")
            .description("The system name of this feed.  The default is to have this name automatically set when the feed is created.  "
                            + "Normally you do not need to change the default value.")
            .defaultValue("${metadata.systemFeedName}")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
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
        String category = context.getProperty(META_FEED_CATEGORY).getValue();
        String feedName = context.getProperty(META_FEED_NAME).getValue();
        
        try {
            this.feedId = getProviderService(context).getProvider().getFeedId(category, feedName);
        } catch (Exception e) {
            getLogger().warn("Failure retrieving feed metadata" + category + "/" + feedName, e);
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

    private FlowFile createFlowFile(ProcessContext context, 
                                    ProcessSession session,
                                    FeedPreconditionTriggerEvent event) {
        FlowFile file = session.create();
        
        if (this.feedId != null) {
            Map<DateTime, Map<String, String>> props = getProviderService(context).getProvider().getFeedDependentResultDeltas(this.feedId);
            try {
                String value = MAPPER.writeValueAsString(props);
                file = session.putAttribute(file, ComponentAttributes.FEED_DEPENDENT_RESULT_DELTAS.name(), value);
            } catch (JsonProcessingException e) {
                getLogger().warn("Failed to serialize feed dependency result deltas", e);
                // TODO Swallow the exception and produce the flow file anyway?
            }
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
                getLogger().debug("Precondition event triggered: {}", new Object[]{ event });
                
                TriggerFeed.this.triggerEventQueue.add(event);
            }
        };
        return listener;
    }

}
