/**
 *
 */
package com.thinkbiganalytics.nifi.processors.metadata;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
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
import com.thinkbiganalytics.metadata.rest.model.sla.DatasourceUpdatedSinceFeedExecutedMetric;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;

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
    private AtomicReference<String> feedId = new AtomicReference<>();
    private Set<Datasource> sourceDatasources = Collections.synchronizedSet(new HashSet<Datasource>());
    
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
    
    public static final PropertyDescriptor SRC_DATASOURCES_NAME = new PropertyDescriptor.Builder()
            .name(SRC_DATASET_ID_PROP)
            .displayName("Source datasource name")
            .description("The name of the datasource that this feed will read from (optional)")
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

        String datasourcesName = context.getProperty(SRC_DATASOURCES_NAME).getValue();

        if (! StringUtils.isEmpty(datasourcesName)) {
            String[] dsNameArr = datasourcesName.split("\\s*,\\s*");
            
            for (String dsName : dsNameArr) {
                setupSource(context, feed, dsName.trim());
            }
            
            ensurePreconditonListener(context, feed, dsNameArr);
        }
    }
    
    protected void setupSource(ProcessContext context, Feed feed, String datasourceName) {
        MetadataProvider provider = getProviderService(context).getProvider();
        Datasource datasource = getSourceDatasource(context, datasourceName);

        if (datasource != null) {
            getLogger().debug("ensuring feed source - feed: {} datasource: {}", new Object[] { this.feedId.get(), datasource.getId() });
            provider.ensureFeedSource(this.feedId.get(), datasource.getId());
            this.sourceDatasources.add(datasource);
        } else {
            throw new ProcessException("Source datasource does not exist: " + datasourceName);
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
//        props.add(PRECONDITION_NAME);
        props.add(SRC_DATASOURCES_NAME);
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
    
    private void ensurePreconditonListener(ProcessContext context, Feed feed, String[] dsNames) {
        if (this.preconditionListener == null) {
            MetadataProvider provider = getProviderService(context).getProvider();
            FeedPreconditionEventService precondService = getPreconditionService(context);
            
            PreconditionListener listener = new PreconditionListener() {
                @Override
                public void triggered(DatasourceChangeEvent event) {
                    getLogger().debug("Precondition event triggered - feed: {}, datasets: {}", 
                            new Object[] { event.getFeed().getSystemName(), event.getDatasets() });
                    List<Dataset> datasets = event.getDatasets();
                    FeedBegin.this.pendingChanges.add(datasets);
                }
            };
        
            // If no precondition exits yet install one that depends on the datasources.
            if (feed.getPrecondition() == null) {
                getLogger().debug("Setting default feed preconditions for: " + dsNames);
                
                Metric[] metrics = new Metric[dsNames.length];
                
                for (int idx = 0; idx < metrics.length; idx++) {
                    DatasourceUpdatedSinceFeedExecutedMetric metric = new DatasourceUpdatedSinceFeedExecutedMetric();
                    metric.setFeedName(feed.getSystemName());
                    metric.setDatasourceName(dsNames[idx]);
                    metrics[idx] = metric;
                }
                
                provider.ensurePrecondition(feed.getId(), metrics);
            }
            
            for (String dsName : dsNames) {
                getLogger().debug("Adding precondition listener for datasoure name: " + dsName);
                precondService.addListener(dsName, listener);
            }
            
            this.preconditionListener = listener;
        }
    }

    protected Datasource getSourceDatasource(ProcessContext context, String datasourceName) {
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
