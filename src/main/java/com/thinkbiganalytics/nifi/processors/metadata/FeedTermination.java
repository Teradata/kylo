/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.util.List;
import java.util.Set;

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
    
    public static final PropertyDescriptor DEST_DATASET_NAME = new PropertyDescriptor.Builder()
            .name(DEST_DATASET_ID_PROP)
            .displayName("Destination dataset name")
            .description("The unique name of the dataset that the feed will update")
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

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("Success")
            .description("Relationship followdd on successful metadata capture.")
            .build();

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        try {
            FlowFile flowFile = session.get();
            DatasetProvider datasetProvider = getProviderService(context).getDatasetProvider();
            DataOperationsProvider operationProvider = getProviderService(context).getDataOperationsProvider();
            
            Dataset destDataset = ensureDestinationDataset(context);    
            FeedDestination dest = ensureFeedDestination(context, destDataset);
            String timeStr = flowFile.getAttribute(OPERATON_START_PROP);
            DateTime opStart = timeStr != null ? TIME_FORMATTER.parseDateTime(timeStr) : new DateTime();
            
            DataOperation op = operationProvider.beginOperation(dest, opStart);
            operationProvider.updateOperation(op.getId(), "", getOperationState(context));
            
            flowFile = session.putAttribute(flowFile, OPERATON_STOP_PROP, TIME_FORMATTER.print(new DateTime()));
            
            session.transfer(flowFile, SUCCESS);
        } catch (Exception e) {
            context.yield();
            session.rollback();
            getLogger().error("Unexpected error processing feed completion", e);
            throw new ProcessException(e);
        }
    }

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(COMPLETION_RESULT);
    }
    
    @Override
    protected void addRelationships(Set<Relationship> rels) {
        super.addRelationships(rels);
        rels.add(SUCCESS);
    }

    private State getOperationState(ProcessContext context) {
        String result = context.getProperty(COMPLETION_RESULT).getValue();
        
        if (result.equals("FAILURE")) {
            return State.FAILURE;
        } else {
            return State.SUCCESS;
        }
    }
    
    protected abstract Dataset createDestinationDataset(ProcessContext context, String name, String descr);
    
    protected Dataset ensureDestinationDataset(ProcessContext context) {
        DatasetProvider datasetProvider = getProviderService(context).getDatasetProvider();
        String datasetName = context.getProperty(DEST_DATASET_NAME).getValue();
        DatasetCriteria crit = datasetProvider.datasetCriteria().name(datasetName);
        Set<Dataset> datasets = datasetProvider.getDatasets(crit);
        
        if (datasets.size() > 0) {
            return datasets.iterator().next();
        } else {
            getLogger().info("No dataset exists with the givn name, creating: " + datasetName);
            
            return createDestinationDataset(context, datasetName, "");
        }
    }
    
    protected FeedDestination ensureFeedDestination(ProcessContext context, Dataset destDataset) {
        FeedProvider feedProvider = getProviderService(context).getFeedProvider();
        String feedIdStr = context.getProperty(FEED_ID_PROP).getValue();
        Feed.ID feedId = feedProvider.asFeedId(feedIdStr);
        
        return feedProvider.ensureFeedDestination(feedId, destDataset.getId());
    }

}
