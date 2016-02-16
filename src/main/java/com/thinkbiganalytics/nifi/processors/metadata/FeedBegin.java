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
import org.joda.time.format.ISODateTimeFormat;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperation;

/**
 *
 * @author Sean Felten
 */
public class FeedBegin extends FeedProcessor {
    
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

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("Success")
            .description("FlowFiles are rounted to this relationship on successful metadata capture.")
            .build();

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        FeedProvider feedProvider = getProviderService(context).getFeedProvider();
        String feedName = context.getProperty(FEED_NAME).getValue();
        Feed feed = feedProvider.ensureFeed(feedName, "");
        Dataset srcDataset = getSourceDataset(context);            
        
        if (srcDataset != null) {
            feedProvider.ensureFeedSource(feed.getId(), srcDataset.getId());
            // TODO Register interest in metadata events for this source
        }
        
        flowFile = session.putAttribute(flowFile, FEED_ID_PROP, feed.getId().toString());
        flowFile = session.putAttribute(flowFile, OPERATON_START_PROP, TIME_FORMATTER.print(new DateTime()));
        
        session.transfer(flowFile, SUCCESS);
    }

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(FEED_NAME);
        props.add(SRC_DATASET_NAME);
    }
    
    @Override
    protected void addRelationships(Set<Relationship> rels) {
        super.addRelationships(rels);
        rels.add(SUCCESS);
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

}
