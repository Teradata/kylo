/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import java.io.Serializable;
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

import com.thinkbiganalytics.controller.precond.FeedPrecondition.ID;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;

/**
 *
 * @author Sean Felten
 */
public class InMemoryFeedPreconditionService extends AbstractControllerService implements FeedPreconditionService {

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
    
    private ServiceLevelAgreementProvider privider;
    private Map<FeedPrecondition.ID, Set<PreconditionListener>> listeners = new ConcurrentHashMap<>();
    
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
        PropertyValue impl = context.getProperty(IMPLEMENTATION);
        
        if (impl.getValue().equalsIgnoreCase("MEMORY")) {
            this.privider = new InMemorySLAProvider();
        } else {
            throw new UnsupportedOperationException("Provider implementations currently not supported: " + impl.getValue());
        }
    }
    
    @Override
    public ID resolve(Serializable ser) {
        ServiceLevelAgreement.ID slaId = this.privider.resolve(ser);
        return new PrecondId(slaId);
    }
    
    @Override
    public FeedPrecondition createPrecondition(String name, Metric first, Metric... rest) {
        ServiceLevelAgreement sla = this.privider.builder()
                .name(name)
                .obligationBuilder()
                .metric(first, rest)
                .add()
                .build();
        
        return new FeedPreconditionImpl(sla);
    }

    @Override
    public FeedPrecondition createPrecondition(String name, Collection<Metric> metrics) {
        ServiceLevelAgreement sla = this.privider.builder()
                .name(name)
                .obligationBuilder()
                .metric(metrics)
                .add()
                .build();
        
        return new FeedPreconditionImpl(sla);
    }
    
    

    @Override
    public FeedPrecondition getPrecondition(ID id) {
        PrecondId pid = (PrecondId) id;
        ServiceLevelAgreement sla = this.privider.getAgreement(pid.slaId);
        
        if (sla != null) {
            return new FeedPreconditionImpl(sla);
        } else {
            return null;
        }
    }

    @Override
    public FeedPrecondition getPrecondition(String name) {
        ServiceLevelAgreement sla = this.privider.findAgreementByName(name);
        
        if (sla != null) {
            return new FeedPreconditionImpl(sla);
        } else {
            return null;
        }
    }

    @Override
    public Set<ChangeSet<? extends Dataset, ? extends ChangedContent>> checkPreconditions(ID slaId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addListener(ID id, PreconditionListener listener) {
        Set<PreconditionListener> set = this.listeners.get(id);
        if (set == null) {
            set = new HashSet<>();
            this.listeners.put(id, set);
        }
        set.add(listener);
    }
    
    @Override
    public void addListener(String name, PreconditionListener listener) {
        ServiceLevelAgreement sla = this.privider.findAgreementByName(name);
        if (sla != null) {
            addListener(new PrecondId(sla.getId()), listener);
        } else {
            throw new ProcessException("No precondition exists with the name: " + name);
        }

    }
    
    public void setPrivider(ServiceLevelAgreementProvider privider) {
        this.privider = privider;
    }
    
    
    private ServiceLevelAgreementProvider getProvider() {
        return this.privider;
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
}
