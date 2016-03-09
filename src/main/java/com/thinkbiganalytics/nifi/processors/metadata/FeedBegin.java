/**
 *
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
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
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.joda.time.DateTime;

import com.thinkbiganalytics.controller.metadata.MetadataProvider;
import com.thinkbiganalytics.controller.precond.FeedPreconditionEventService;
import com.thinkbiganalytics.controller.precond.PreconditionListener;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.event.DatasourceChangeEvent;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;

/**
 * @author Sean Felten
 */
@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"feed", "begin", "thinkbig"})
@CapabilityDescription("Records the start of a feed to be tracked and listens to events which may trigger a flow. This processor should be either the first processor or immediately follow the first processor in a flow.")
public class FeedBegin extends FeedProcessor {
    
    private Queue<List<Dataset>> pendingChanges = new LinkedBlockingQueue<>();
    private PreconditionListener preconditionListener;
//    // TODO remove this
//    private DataChangeEventListener<? extends Dataset, ? extends ChangedContent> changeListener;
//    // TODO remove this
//    private Queue<ChangeSet<? extends Dataset, ? extends ChangedContent>> pendingChange = new LinkedBlockingQueue<>();
    private AtomicReference<String> sourceDatasetId = new AtomicReference<>();
    private AtomicReference<String> feedId = new AtomicReference<>();
    
    public static final PropertyDescriptor PRECONDITION_SERVICE = new PropertyDescriptor.Builder()
            .name("Feed Precondition Event Service")
            .description("Service that manages preconditions that trigger feed execution")
            .required(false)
            .identifiesControllerService(FeedPreconditionEventService.class)
            .build();

    public static final PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
            .name(FEED_ID_PROP)
            .displayName("Feed name")
            .description("The unique name of the feed that is beginning")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor PRECONDITION_NAME = new PropertyDescriptor.Builder()
            .name("PRECONDITION")
            .displayName("Precondition name")
            .description("The unique name of the precondition that will trigger this feed's execution")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final PropertyDescriptor SRC_DATASOURCE_NAME = new PropertyDescriptor.Builder()
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
            .description("Relationship followed on failed metadata capture.")
            .build();
    

    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);
    }

    @OnScheduled
    public void setupFeedMetadata(ProcessContext context) {
        MetadataProvider provider = getProviderService(context).getProvider();
        String feedName = context.getProperty(FEED_NAME).getValue();
        Feed feed = provider.ensureFeed(feedName, "");
        this.feedId.set(feed.getId());

        String datasetName = context.getProperty(SRC_DATASOURCE_NAME).getValue();

        if (datasetName != null && this.sourceDatasetId.get() == null) {
            Datasource srcDatasource = getSourceDatasource(context);

            if (srcDatasource != null) {
                provider.ensureFeedSource(this.feedId.get(), srcDatasource.getId());
                ensurePreconditonListener(context);
                this.sourceDatasetId.set(srcDatasource.getId());
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

    @Override
    protected void addProperties(List<PropertyDescriptor> props) {
        super.addProperties(props);
        props.add(PRECONDITION_SERVICE);
        props.add(FEED_NAME);
        props.add(PRECONDITION_NAME);
        props.add(SRC_DATASOURCE_NAME);
    }

    @Override
    protected void addRelationships(Set<Relationship> rels) {
        super.addRelationships(rels);
        rels.add(SUCCESS);
        rels.add(FAILURE);
    }

    protected FeedPreconditionEventService getPreconditionService(ProcessContext context) {
        return context.getProperty(PRECONDITION_SERVICE).asControllerService(FeedPreconditionEventService.class);
    }

    protected FlowFile produceFlowFile(ProcessSession session) {
        List<Dataset> changes = this.pendingChanges.poll();
        if (changes != null) {
            return createFlowFile(session, changes);
        } else {
            return session.get();
        }
    }

    private FlowFile createFlowFile(ProcessSession session, 
                                    List<Dataset> changes) {
        // TODO add changes to flow file
        return session.create();
    }
    
    private void ensurePreconditonListener(ProcessContext context) {
        FeedPreconditionEventService precondService = getPreconditionService(context);
        String precondName = context.getProperty(PRECONDITION_NAME).getValue();
        
        if (this.preconditionListener == null) {
            PreconditionListener listener = new PreconditionListener() {
                @Override
                public void triggered(DatasourceChangeEvent event) {
                    List<Dataset> datasets = event.getDatasets();
                    FeedBegin.this.pendingChanges.add(datasets);
                }
            };
            
            this.preconditionListener = listener;
            precondService.addListener(precondName, listener);
        }
    }
//
//    private void ensureChangeListener(ProcessContext context, Dataset srcDataset) {
//        DataOperationsProvider operationProvider = getProviderService(context).getDataOperationsProvider();
//
//        if (this.changeListener == null) {
//            // TODO Hmmmm....
//            if (srcDataset instanceof DirectoryDataset) {
//                DataChangeEventListener<DirectoryDataset, FileList> listener = new DataChangeEventListener<DirectoryDataset, FileList>() {
//                    @Override
//                    public void handleEvent(DataChangeEvent<DirectoryDataset, FileList> event) {
//                        getLogger().debug("Dependent dataset changed: {} - {}",
//                                new Object[]{event.getChangeSet().getDataset(), event.getChangeSet().getChanges()});
//                        recordDirectoryChange(event.getChangeSet());
//                    }
//                };
//
//                operationProvider.addListener((DirectoryDataset) srcDataset, listener);
//                this.changeListener = listener;
//            } else if (srcDataset instanceof HiveTableDataset) {
//                DataChangeEventListener<HiveTableDataset, HiveTableUpdate> listener = new DataChangeEventListener<HiveTableDataset, HiveTableUpdate>() {
//                    @Override
//                    public void handleEvent(DataChangeEvent<HiveTableDataset, HiveTableUpdate> event) {
//                        getLogger().debug("Dependent dataset changed: {} - {}",
//                                new Object[]{event.getChangeSet().getDataset(), event.getChangeSet().getChanges()});
//                        recordTableChange(event.getChangeSet());
//                    }
//                };
//
//                operationProvider.addListener((HiveTableDataset) srcDataset, listener);
//                this.changeListener = listener;
//            } else {
//                throw new ProcessException("Unsupported dataset type: " + srcDataset.getClass());
//            }
//        }
//    }
//
//    protected void recordDirectoryChange(ChangeSet<DirectoryDataset, FileList> changeSet) {
//        this.pendingChange.add(changeSet);
//    }
//
//    protected void recordTableChange(ChangeSet<HiveTableDataset, HiveTableUpdate> changeSet) {
//        this.pendingChange.add(changeSet);
//    }

    protected Datasource getSourceDatasource(ProcessContext context) {
        String datasourceName = context.getProperty(SRC_DATASOURCE_NAME).getValue();

        if (datasourceName != null) {
            Datasource dataset = findDatasource(context, datasourceName);

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
