/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.AllowableValue;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.controller.metadata.MetadataProviderService;
import com.thinkbiganalytics.controller.precond.FeedPrecondition.ID;
import com.thinkbiganalytics.controller.precond.metric.DatasetUpdatedSinceMetricAssessor;
import com.thinkbiganalytics.controller.precond.metric.DependentFeedMetric;
import com.thinkbiganalytics.controller.precond.metric.FeedExecutedSinceFeedMetricAssessor;
import com.thinkbiganalytics.controller.precond.metric.WithinScheduleAssessor;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.event.DataChangeEvent;
import com.thinkbiganalytics.metadata.api.event.DataChangeEventListener;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationAssessment;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.SimpleServiceLevelAssessor;

/**
 *
 * @author Sean Felten
 */
public class InMemoryFeedPreconditionService extends AbstractControllerService implements FeedPreconditionService {

    public static final PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
            .name("Metadata Provider Service")
            .description("Specified Service supplying the implemtentions of the various metadata providers")
            .required(true)
            .identifiesControllerService(MetadataProviderService.class)
            .build();
    
    private static final AllowableValue[] ALLOWABLE_IMPLEMENATIONS = {
            new AllowableValue("MEMORY", "In-memory storage", "An implemenation that managers preconditions in memory (for development-only)"),
            new AllowableValue("SERVICE", "Client", "An implemenation that accesses metadata via the precondition service client")
    };
    
    public static final PropertyDescriptor IMPLEMENTATION = new PropertyDescriptor.Builder()
            .name("Implementation")
            .description("Specifies which client should be used to manage preconditions")
            .allowableValues(ALLOWABLE_IMPLEMENATIONS)
            .defaultValue("MEMORY")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    private static final List<PropertyDescriptor> properties = Collections.singletonList(IMPLEMENTATION);
    
    private MetadataProviderService metadataService;
    private ServiceLevelAgreementProvider slaProvider;
    private ServiceLevelAssessor assessor;
    private Map<ServiceLevelAgreement.ID, Set<PreconditionListener>> listeners = new ConcurrentHashMap<>();
    
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        this.metadataService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        PropertyValue impl = context.getProperty(IMPLEMENTATION);

        if (impl.getValue().equalsIgnoreCase("MEMORY")) {
            this.slaProvider = new InMemorySLAProvider();
        } else {
            throw new UnsupportedOperationException("Provider implementations currently not supported: " + impl.getValue());
        }
        
        this.assessor = new SimpleServiceLevelAssessor();
        addAssessors(this.assessor, context);
    }
    
    @Override
    public ID resolve(Serializable ser) {
        ServiceLevelAgreement.ID slaId = this.slaProvider.resolve(ser);
        return new PrecondId(slaId);
    }
    
    @Override
    public FeedPrecondition createPrecondition(String name, Metric first, Metric... rest) {
        ServiceLevelAgreement sla = this.slaProvider.builder()
                .name(name)
                .obligationBuilder()
                .metric(first, rest)
                .add()
                .build();
        
        listenFeeds(first, rest);
        return new FeedPreconditionImpl(sla);
    }

    @Override
    public FeedPrecondition createPrecondition(String name, Collection<Metric> metrics) {
        ServiceLevelAgreement sla = this.slaProvider.builder()
                .name(name)
                .obligationBuilder()
                .metric(metrics)
                .add()
                .build();
        
        listenFeeds(metrics);
        return new FeedPreconditionImpl(sla);
    }
    
    @Override
    public FeedPrecondition getPrecondition(ID id) {
        PrecondId pid = (PrecondId) id;
        ServiceLevelAgreement sla = this.slaProvider.getAgreement(pid.slaId);
        
        if (sla != null) {
            return new FeedPreconditionImpl(sla);
        } else {
            return null;
        }
    }

    @Override
    public FeedPrecondition getPrecondition(String name) {
        ServiceLevelAgreement sla = this.slaProvider.findAgreementByName(name);
        
        if (sla != null) {
            return new FeedPreconditionImpl(sla);
        } else {
            return null;
        }
    }

    @Override
    public List<ChangeSet<? extends Dataset, ? extends ChangedContent>> checkPreconditions(ID id) {
        PrecondId idImpl = (PrecondId) id;
        ServiceLevelAgreement sla = this.slaProvider.getAgreement(idImpl.slaId);
        
        if (sla != null) {
            return checkPrecondition(sla);
        } else {
            throw new ProcessException("No precondition exists with the ID: " + id);
        }
    }
    
    @Override
    public Set<FeedPrecondition> getPreconditions() {
        List<ServiceLevelAgreement> list = this.slaProvider.getAgreements();
        return Sets.newHashSet(Iterables.transform(list, new Function<ServiceLevelAgreement, FeedPrecondition>() {
            @Override
            public FeedPrecondition apply(ServiceLevelAgreement sla) {
                return new FeedPreconditionImpl(sla);
            }
        }));
    }

    @Override
    public void addListener(ID id, PreconditionListener listener) {
        ServiceLevelAgreement.ID slaId = ((PrecondId) id).slaId;
        Set<PreconditionListener> set = this.listeners.get(slaId);
        if (set == null) {
            set = new HashSet<>();
            this.listeners.put(slaId, set);
        }
        set.add(listener);
    }
    
    @Override
    public void addListener(String name, PreconditionListener listener) {
        ServiceLevelAgreement sla = this.slaProvider.findAgreementByName(name);
        if (sla != null) {
            addListener(new PrecondId(sla.getId()), listener);
        } else {
            throw new ProcessException("No precondition exists with the name: " + name);
        }

    }
    
    public void setPrivider(ServiceLevelAgreementProvider privider) {
        this.slaProvider = privider;
    }
    
    
    private void listenFeed(String feedName) {
        FeedProvider fPvdr = this.metadataService.getFeedProvider();
        DataOperationsProvider dPvdr = this.metadataService.getDataOperationsProvider();
        Collection<Feed> feeds = fPvdr.getFeeds(fPvdr.feedCriteria().name(feedName));
        
        if (! feeds.isEmpty()) {
            Feed feed = feeds.iterator().next();
            for (FeedDestination dest : feed.getDestinations()) {
                Dataset ds = dest.getDataset();
                dPvdr.addListener(ds, createDataChangeListener());
            }
        }
    }

    private DataChangeEventListener<Dataset, ChangedContent> createDataChangeListener() {
        return new DataChangeEventListener<Dataset, ChangedContent>() {
            @Override
            public void handleEvent(DataChangeEvent<Dataset, ChangedContent> event) {
                for (ServiceLevelAgreement sla : getSlaProvider().getAgreements()) {
                    List<ChangeSet<? extends Dataset, ? extends ChangedContent>> changes = checkPrecondition(sla);
                    
                    if (changes != null) {
                        for (PreconditionListener listener : getPreconditionListeners().get(sla.getId())) {
                            PreconditionEvent preEv = new PreconditionEventImpl(new PrecondId(sla.getId()), changes);
                            listener.triggered(preEv);
                        }
                    }
                }
            }
        };
    }

    private ServiceLevelAgreementProvider getSlaProvider() {
        return InMemoryFeedPreconditionService.this.slaProvider;
    }
    
    protected Map<ServiceLevelAgreement.ID, Set<PreconditionListener>> getPreconditionListeners() {
        return InMemoryFeedPreconditionService.this.listeners;
    }

    private void listenFeeds(Collection<Metric> metrics) {
        for (Metric metric : metrics) {
            if (metric instanceof DependentFeedMetric) {
                listenFeed(((DependentFeedMetric) metric).getFeedName());
            }
        }
    }

    private void listenFeeds(Metric first, Metric[] rest) {
        if (first instanceof DependentFeedMetric) {
            listenFeed(((DependentFeedMetric) first).getFeedName());
        }
        
        for (Metric metric : rest) {
            if (metric instanceof DependentFeedMetric) {
                listenFeed(((DependentFeedMetric) metric).getFeedName());
            }
        }
    }

    private List<ChangeSet<? extends Dataset, ? extends ChangedContent>> checkPrecondition(ServiceLevelAgreement sla) {
        ServiceLevelAssessment assmt = this.assessor.assess(sla);
        
        if (assmt.getResult() != AssessmentResult.FAILURE) {
            return collectResults(assmt);
        } else {
            return null;
        }
    }

    private List<ChangeSet<? extends Dataset, ? extends ChangedContent>> collectResults(ServiceLevelAssessment assmt) {
        List<ChangeSet<? extends Dataset, ? extends ChangedContent>> result = new ArrayList<>();
        
        for (ObligationAssessment obAssmt : assmt.getObligationAssessments()) {
            for (MetricAssessment<ArrayList<ChangeSet<Dataset, ChangedContent>>> mAssmt 
                    : obAssmt.<ArrayList<ChangeSet<Dataset, ChangedContent>>>getMetricAssessments()) {
                result.addAll(mAssmt.getData());
            }
        }
        
        return result;
    }

    private void addAssessors(ServiceLevelAssessor assr, ConfigurationContext context) {
        MetadataProviderService metaSvc = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);
        assr.registerMetricAssessor(new FeedExecutedSinceFeedMetricAssessor(metaSvc));
        assr.registerMetricAssessor(new DatasetUpdatedSinceMetricAssessor(metaSvc));
        assr.registerMetricAssessor(new WithinScheduleAssessor(metaSvc));
    }

    private ServiceLevelAgreementProvider getProvider() {
        return this.slaProvider;
    }
    
    
    private static class PrecondId implements FeedPrecondition.ID {
        private final ServiceLevelAgreement.ID slaId;
        
        public PrecondId(ServiceLevelAgreement.ID slaId) {
            this.slaId = slaId;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PrecondId) {
                return ((PrecondId) obj).slaId.equals(this.slaId);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return this.slaId.hashCode();
        }
    }
    
    private static class FeedPreconditionImpl implements FeedPrecondition {
        private ServiceLevelAgreement sla;
        private PrecondId id;
        
        public FeedPreconditionImpl(ServiceLevelAgreement sla) {
            this.id = new PrecondId(sla.getId());
            this.sla = sla;
        }
        
        @Override
        public ID getId() {
            return this.id;
        }

        @Override
        public String getName() {
            return this.sla.getName();
        }

        @Override
        public Set<Metric> getMetrics() {
            Set<Metric> set = new HashSet<>();
            for (Obligation ob : this.sla.getObligations()) {
                set.addAll(ob.getMetrics());
            }
            return set;
        }
    }
    
    private static class PreconditionEventImpl implements PreconditionEvent {
        
        private FeedPrecondition.ID id;
        private List<ChangeSet<? extends Dataset, ? extends ChangedContent>> changes;

        public PreconditionEventImpl(PrecondId precondId,
                                     List<ChangeSet<? extends Dataset, ? extends ChangedContent>> changes) {
            this.id = precondId;
            this.changes = changes;
        }

        @Override
        public ID getPreconditonId() {
            return this.id;
        }

        @Override
        public List<ChangeSet<? extends Dataset, ? extends ChangedContent>> getChanges() {
            return this.changes;
        }
    }
}
