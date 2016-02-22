/**
 *
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.DirectoryDataset;
import com.thinkbiganalytics.metadata.api.dataset.filesys.FileList;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableDataset;
import com.thinkbiganalytics.metadata.api.dataset.hive.HiveTableUpdate;
import com.thinkbiganalytics.metadata.api.event.DataChangeEvent;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
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
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Sean Felten
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "begin", "thinkbig"})
@CapabilityDescription("Records the start of a feed to be tracked and listens to events which may trigger a flow. This processor should be either the first processor or immediately follow the first processor in a flow.")
public class FeedBegin extends FeedProcessor {

    private Queue<ChangeSet<? extends Dataset, ? extends ChangedContent>> pendingChanges = new LinkedBlockingQueue<>();
    private DataChangeEventListener<? extends Dataset, ? extends ChangedContent> changeListener;
    private AtomicReference<Dataset.ID> sourceDatasetId = new AtomicReference<Dataset.ID>();
    private AtomicReference<Feed.ID> feedId = new AtomicReference<>();

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
            .description("The unique name of the dataset that the feed will read from (optional)")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("Success")
            .description("Relationship followed on successful metadata capture.")
            .build();
    public static final Relationship FAILURE = new Relationship.Builder()
            .name("Failure")
            .description("Relationship followdd on failed metadata capture.")
            .build();

    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);
    }

    @OnScheduled
    public void setupFeedMetadata(ProcessContext context) {
        FeedProvider feedProvider = getProviderService(context).getFeedProvider();
        String feedName = context.getProperty(FEED_NAME).getValue();
        Feed feed = feedProvider.ensureFeed(feedName, "");
        this.feedId.set(feed.getId());

        String datasetName = context.getProperty(SRC_DATASET_NAME).getValue();

        if (datasetName != null && this.sourceDatasetId.get() == null) {
            Dataset srcDataset = getSourceDataset(context);

            if (srcDataset != null) {
                feedProvider.ensureFeedSource(this.feedId.get(), srcDataset.getId());
                ensureChangeListener(context, srcDataset);
                this.sourceDatasetId.set(srcDataset.getId());
            } else {
                throw new ProcessException("Source dataset does not exist: " + datasetName);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = produceFlowFile(session);

        while (flowFile != null) {
            flowFile = session.putAttribute(flowFile, FEED_ID_PROP, this.feedId.get().toString());
            flowFile = session.putAttribute(flowFile, OPERATON_START_PROP, TIME_FORMATTER.print(new DateTime()));

            session.transfer(flowFile, SUCCESS);

            flowFile = produceFlowFile(session);
            if (flowFile == null) {
                context.yield();
            }
        }
    }

    protected FlowFile produceFlowFile(ProcessSession session) {
        ChangeSet<? extends Dataset, ? extends ChangedContent> changeSet = this.pendingChanges.poll();
        if (changeSet != null) {
            return createFlowFile(session, changeSet);
        } else {
            return session.get();
        }
    }

    private FlowFile createFlowFile(ProcessSession session,
                                    ChangeSet<? extends Dataset, ? extends ChangedContent> changeSet) {
        return session.create();
    }

    private void ensureChangeListener(ProcessContext context, Dataset srcDataset) {
        DataOperationsProvider operationProvider = getProviderService(context).getDataOperationsProvider();

        if (this.changeListener == null) {
            // TODO Hmmmm....
            if (srcDataset instanceof DirectoryDataset) {
                DataChangeEventListener<DirectoryDataset, FileList> listener = new DataChangeEventListener<DirectoryDataset, FileList>() {
                    @Override
                    public void handleEvent(DataChangeEvent<DirectoryDataset, FileList> event) {
                        getLogger().debug("Dependent dataset changed: {} - {}",
                                new Object[]{event.getChangeSet().getDataset(), event.getChangeSet().getChanges()});
                        recordDirectoryChange(event.getChangeSet());
                    }
                };

                operationProvider.addListener((DirectoryDataset) srcDataset, listener);
                this.changeListener = listener;
            } else if (srcDataset instanceof HiveTableDataset) {
                DataChangeEventListener<HiveTableDataset, HiveTableUpdate> listener = new DataChangeEventListener<HiveTableDataset, HiveTableUpdate>() {
                    @Override
                    public void handleEvent(DataChangeEvent<HiveTableDataset, HiveTableUpdate> event) {
                        getLogger().debug("Dependent dataset changed: {} - {}",
                                new Object[]{event.getChangeSet().getDataset(), event.getChangeSet().getChanges()});
                        recordTableChange(event.getChangeSet());
                    }
                };

                operationProvider.addListener((HiveTableDataset) srcDataset, listener);
                this.changeListener = listener;
            } else {
                throw new ProcessException("Unsupported dataset type: " + srcDataset.getClass());
            }
        }
    }

    protected void recordDirectoryChange(ChangeSet<DirectoryDataset, FileList> changeSet) {
        this.pendingChanges.add(changeSet);
    }

    protected void recordTableChange(ChangeSet<HiveTableDataset, HiveTableUpdate> changeSet) {
        this.pendingChanges.add(changeSet);
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
        rels.add(FAILURE);
    }

    protected Dataset getSourceDataset(ProcessContext context) {
        String datasetName = context.getProperty(SRC_DATASET_NAME).getValue();

        if (datasetName != null) {
            Dataset dataset = findDataset(context, datasetName);

            if (dataset != null) {
                return dataset;
            } else {
                return null;
            }
        } else {
            return null;
        }

    }

}
