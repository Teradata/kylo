/**
 *
 */
package com.thinkbiganalytics.nifi.v2.metadata;

import static com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants.FEED_NAME_PROP;
import static com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants.OPERATON_START_PROP;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

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

import com.thinkbiganalytics.metadata.rest.model.Formatters;
import com.thinkbiganalytics.metadata.rest.model.event.FeedPreconditionTriggerEvent;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataConstants;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.precondition.FeedPreconditionEventService;
import com.thinkbiganalytics.nifi.core.api.precondition.PreconditionListener;

/**
 * @author Sean Felten
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "trigger", "thinkbig"})
@CapabilityDescription("Triggers the execution of a feed whenever the conditions defined by its precondition have been met.  This process should be the first processor in a flow depends upon preconditions.")
public class TriggerFeed extends AbstractFeedProcessor {

    private Queue<FeedPreconditionTriggerEvent> triggerEventQueue = new LinkedBlockingQueue<>();
    private PreconditionListener preconditionListener;

    public static final PropertyDescriptor PRECONDITION_SERVICE = new PropertyDescriptor.Builder()
            .name("Feed Precondition Event Service")
            .description("Service that manages preconditions which trigger feed execution")
            .required(false)
            .identifiesControllerService(FeedPreconditionEventService.class)
            .build();

    public static final PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
            .name(FEED_NAME_PROP)
            .displayName("Feed name")
            .description("The unique name of the feed that is beginning")
            .defaultValue("${feed.name}")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("Success")
            .description("Relationship followed on successful precondition event.")
            .build();
// TODO: do we need a failure relationship?
//    public static final Relationship FAILURE = new Relationship.Builder()
//            .name("Failure")
//            .description("Relationship followed on failed metadata capture.")
//            .build();


    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);
        
        this.preconditionListener = createPreconditionListener();
    }

    @OnScheduled
    public void scheduled(ProcessContext context) {
        String feedName = context.getProperty(FEED_NAME).getValue();
        registerPreconditonListener(context, feedName);
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = produceFlowFile(session);

        while (flowFile != null) {
            String feedName = context.getProperty(FEED_NAME).getValue();

            flowFile = session.putAttribute(flowFile, MetadataConstants.FEED_NAME_PROP, feedName);
            flowFile = session.putAttribute(flowFile, OPERATON_START_PROP, Formatters.TIME_FORMATTER.print(new DateTime()));

            session.transfer(flowFile, SUCCESS);

            flowFile = produceFlowFile(session);
            if (flowFile == null) {
                context.yield();
            }
        }
    }

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(PRECONDITION_SERVICE);
        props.add(FEED_NAME);
    }

    @Override
    protected void addRelationships(Set<Relationship> rels) {
        super.addRelationships(rels);
        rels.add(SUCCESS);
//        rels.add(FAILURE);
    }

    protected FeedPreconditionEventService getPreconditionService(ProcessContext context) {
        return context.getProperty(PRECONDITION_SERVICE).asControllerService(FeedPreconditionEventService.class);
    }

    protected FlowFile produceFlowFile(ProcessSession session) {
        FeedPreconditionTriggerEvent event = this.triggerEventQueue.poll();
        if (event != null) {
            return createFlowFile(session, event);
        } else {
            return session.get();
        }
    }

    private FlowFile createFlowFile(ProcessSession session,
                                    FeedPreconditionTriggerEvent event) {
        // TODO add changes to flow file
        return session.create();
    }

    private void registerPreconditonListener(ProcessContext context, String feedName) {
        FeedPreconditionEventService precondService = getPreconditionService(context);

        precondService.addListener(feedName, preconditionListener);
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
