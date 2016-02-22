/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetCriteria;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;

/**
 *
 * @author Sean Felten
 */
public abstract class FeedTermination extends FeedProcessor {
    
    private AtomicReference<Dataset> destinationDataset = new AtomicReference<>();
    private AtomicReference<FeedDestination> feedDestination = new AtomicReference<>();
    
    public static final PropertyDescriptor DEST_DATASET_NAME = new PropertyDescriptor.Builder()
            .name(DEST_DATASET_ID_PROP)
            .displayName("Destination destinationDataset name")
            .description("The unique name of the destinationDataset that the feed will update")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor COMPLETION_RESULT = new PropertyDescriptor.Builder()
            .name("completion_result")
            .displayName("Completion result of this feed terminating processor")
            .description("The completion result of the feed")
            .allowableValues("SUCCESS", "FAILURE")
            .required(true)
            .build();

//    public static final Relationship SUCCESS = new Relationship.Builder()
//            .name("Success")
//            .description("Relationship followdd on successful metadata capture.")
//            .build();
//    public static final Relationship FAILURE = new Relationship.Builder()
//            .name("Failure")
//            .description("Relationship followdd on failed metadata capture.")
//            .build();

    @OnScheduled
    public void setupDatasetdMetadata(ProcessContext context) {
        if (this.destinationDataset.get() == null) {
            this.destinationDataset.compareAndSet(null, ensureDestinationDataset(context));    
        }
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        try {
            FlowFile flowFile = session.get();
            
            if (flowFile != null) {
                DataOperationsProvider operationProvider = getProviderService(context).getDataOperationsProvider();
                                
                if (this.feedDestination.get() == null) {
                    Dataset ds = this.destinationDataset.get();
                    this.feedDestination.compareAndSet(null, ensureFeedDestination(context, flowFile, ds));
                }
                
                String timeStr = flowFile.getAttribute(OPERATON_START_PROP);
                DateTime opStart = timeStr != null ? TIME_FORMATTER.parseDateTime(timeStr) : new DateTime();
                
                DataOperation op = operationProvider.beginOperation(this.feedDestination.get(), opStart);
                op = completeOperation(context, flowFile, this.destinationDataset.get(), op);
                
                flowFile = session.putAttribute(flowFile, OPERATON_STOP_PROP, TIME_FORMATTER.print(new DateTime()));
                
                session.remove(flowFile);
//            session.transfer(flowFile, SUCCESS);
            } else {
                context.yield();
            }
        } catch (Exception e) {
            context.yield();
            session.rollback();
            getLogger().error("Unexpected error processing feed completion", e);
            throw new ProcessException(e);
        }
    }
    
    protected abstract Dataset createDestinationDataset(ProcessContext context, String name, String descr);

    protected abstract DataOperation completeOperation(ProcessContext context, 
                                                       FlowFile flowFile, 
                                                       Dataset destDataset, 
                                                       DataOperation op);

//    protected abstract ChangeSet<DirectoryDataset, FileList> createChangeSet(ProcessContext context, FlowFile flowFile, Dataset destDataset);
    
    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(DEST_DATASET_NAME);
        props.add(COMPLETION_RESULT);
    }
    
    @Override
    protected void addRelationships(Set<Relationship> rels) {
        super.addRelationships(rels);
//        rels.add(SUCCESS);
//        rels.add(FAILURE);
    }

    private State getOperationState(ProcessContext context) {
        String result = context.getProperty(COMPLETION_RESULT).getValue();
        
        if (result.equals("FAILURE")) {
            return State.FAILURE;
        } else {
            return State.SUCCESS;
        }
    }
    
    protected Dataset ensureDestinationDataset(ProcessContext context) {
        String datasetName = context.getProperty(DEST_DATASET_NAME).getValue();
        Dataset dataset = findDataset(context, datasetName);
        
        if (dataset != null) {
            return dataset;
        } else {
            getLogger().info("No destinationDataset exists with the givn name, creating: " + datasetName);
            
            return createDestinationDataset(context, datasetName, "");
        }
    }
    
    protected FeedDestination ensureFeedDestination(ProcessContext context, FlowFile flowFile, Dataset destDataset) {
        FeedProvider feedProvider = getProviderService(context).getFeedProvider();
        String feedIdStr = flowFile.getAttribute(FEED_ID_PROP);
        
        if (feedIdStr != null) {
            Feed.ID feedId = feedProvider.asFeedId(feedIdStr);
            return feedProvider.ensureFeedDestination(feedId, destDataset.getId());
        } else {
            throw new ProcessException("Feed ID property missing from flow file (" + FEED_ID_PROP + ")");
        }
    }

}
