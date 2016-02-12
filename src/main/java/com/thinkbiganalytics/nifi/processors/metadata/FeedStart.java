/**
 * 
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.util.List;
import java.util.Set;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetCriteria;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;

/**
 *
 * @author Sean Felten
 */
public abstract class FeedStart extends FeedProcessor {
    
    public static final PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
            .name(FEED_ID_PROP)
            .displayName("Feed name")
            .description("The unique name of the feed that is beginning")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor SRC_DATASET_NAME = new PropertyDescriptor.Builder()
            .name(SRC_DATASET_ID_PROP)
            .displayName("Source dataset name")
            .description("The unique name of the dataset that the feed will read from (optinal)")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor DEST_DATASET_NAME = new PropertyDescriptor.Builder()
            .name(DEST_DATASET_ID_PROP)
            .displayName("Destination dataset name")
            .description("The unique name of the dataset that the feed will update")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship PROCEED = new Relationship.Builder()
            .name("Proceed")
            .description("Proceed with flow processing after metadata capture.")
            .build();

    
    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        try {
            FlowFile flowFile = session.get();
            FeedProvider feedProvider = getProviderService(context).getFeedProvider();
            DataOperationsProvider operationProvider = getProviderService(context).getDataOperationsProvider();
            
            Dataset srcDataset = getSourceDataset(context);            
            Dataset destDataset = ensureDestinationDataset(context);            
            String feedName = context.getProperty(FEED_NAME).getValue();
            Feed feed;
            
            if (srcDataset != null) {
                feed = feedProvider.ensureFeed(feedName, feedName, srcDataset.getId(), destDataset.getId());
            } else {
                feed = feedProvider.ensureFeed(feedName, feedName, destDataset.getId());
            }

            DataOperation op = operationProvider.beginOperation(feed.getId(), destDataset.getId());
            
            flowFile = session.putAttribute(flowFile, FEED_ID_PROP, feed.getId().toString());
            flowFile = session.putAttribute(flowFile, OPERATON_ID_PROP, op.getId().toString());
            flowFile = session.putAttribute(flowFile, DATASET_TYPE_PROP, getClass().getSimpleName());
            
            session.transfer(flowFile, PROCEED);
        } catch (Exception e) {
            context.yield();
            session.rollback();
            getLogger().error("Unexpected error processing feed completion", e);
            throw new ProcessException(e);
        }
    }
    
    protected abstract Dataset createDestinationDataset(ProcessContext context, String name, String descr);

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(FEED_NAME);
        props.add(SRC_DATASET_NAME);
        props.add(DEST_DATASET_NAME);
    }
    
    @Override
    protected void addRelationships(Set<Relationship> rels) {
        super.addRelationships(rels);
        rels.add(PROCEED);
    }
    
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
    
    protected Dataset getSourceDataset(ProcessContext context) {
        String datasetName = context.getProperty(SRC_DATASET_NAME).getValue();
        
        if (datasetName != null) {
            Dataset dataset = findDataset(context, datasetName);
            
            if (dataset != null) {
                return dataset;
            } else {
                getLogger().error("No source dataset exists with the givn name: " + datasetName);
                throw new ProcessException("The source dataset does not exist: " + datasetName);
            } 
        } else {
            return null;
        }
        
    }
    
    protected Dataset findDataset(ProcessContext context, String datasetName) {
        DatasetProvider datasetProvider = getProviderService(context).getDatasetProvider();
        DatasetCriteria crit = datasetProvider.datasetCriteria().name(datasetName);
        Set<Dataset> datasets = datasetProvider.getDatasets(crit);
        
        if (datasets.size() > 0) {
            return datasets.iterator().next();
        } else {
            return null;
        }
    }
}
