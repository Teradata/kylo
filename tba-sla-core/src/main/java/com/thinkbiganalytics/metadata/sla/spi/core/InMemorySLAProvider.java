/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID;
import com.thinkbiganalytics.metadata.sla.spi.ObligationBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

/** 
 *
 * @author Sean Felten
 */
public class InMemorySLAProvider implements ServiceLevelAgreementProvider {
    
    private Map<SLAID, ServiceLevelAgreement> slas;

    /**
     * 
     */
    public InMemorySLAProvider() {
        this.slas = Collections.synchronizedMap(new HashMap<SLAID, ServiceLevelAgreement>());
    }
    
    @Override
    public ID resolve(Serializable ser) {
        return resolveImpl(ser);
    }

    @Override
    public List<ServiceLevelAgreement> getAgreements() {
        synchronized (this.slas) {
            return new ArrayList<ServiceLevelAgreement>(this.slas.values());
        }
    }

    @Override
    public ServiceLevelAgreement getAgreement(ID id) {
        return this.slas.get(id);
    }

    @Override
    public ServiceLevelAgreement removeAgreement(ID id) {
        return this.slas.remove(id);
    }
    
    @Override
    public ServiceLevelAgreementBuilder builder() {
        return new SLABuilderImpl();
    }
    
    @Override
    public ServiceLevelAgreementBuilder builder(ID id) {
        return new SLABuilderImpl(resolveImpl(id));
    }
    
    private SLAImpl addSLA(SLAImpl sla) {
        SLAID id = new SLAID();
        sla.setId(id);
        this.slas.put(id, sla);
        return sla;
    }
    
    private SLAImpl replaceSLA(SLAID id, SLAImpl sla) {
        sla.setId(id);
        this.slas.put(id, sla);
        return sla;
    }
    
    private SLAID resolveImpl(Serializable ser) {
        // TODO: throw unknown ID exception?
        if (ser instanceof String) {
            return new SLAID((String) ser);
        } else if (ser instanceof UUID) {
            return new SLAID((UUID) ser);
        } else if (ser instanceof SLAID) { 
            return (SLAID) ser;
        } else {
            throw new IllegalArgumentException("Invalid ID source format: " + ser.getClass());
        }
    }


    
    private class SLABuilderImpl implements ServiceLevelAgreementBuilder {
        
        private SLAID id;
        private String name;
        private String descrtion;
        private Set<Obligation> obligations = new HashSet<Obligation>();
        private SLAImpl sla = new SLAImpl();
        
        public SLABuilderImpl() {
            this(null);
        }
        
        public SLABuilderImpl(SLAID id) {
            this.id = id;
        }

        @Override
        public ServiceLevelAgreementBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public ServiceLevelAgreementBuilder description(String description) {
            this.descrtion = description;
            return this;
        }
        
        @Override
        public ServiceLevelAgreementBuilder obligation(Obligation obligation) {
            this.sla.getObligations().add(obligation);
            return this;
        }

        @Override
        public ObligationBuilder obligationBuilder() {
            return new ObligationBuilderImpl(this.sla, this);
        }
        
        @Override
        public ServiceLevelAgreement build() {
            this.sla.setName(this.name);
            this.sla.setDescription(this.descrtion);
            
            if (this.id == null) {
                return addSLA(sla);
            } else {
                return replaceSLA(this.id, sla);
            }
        }
    }

    private class ObligationBuilderImpl implements ObligationBuilder {
        
        private SLABuilderImpl slaBuilder;
        private SLAImpl sla;
        private String description;
        private Set<Metric> metrics = new HashSet<Metric>();
        
        public ObligationBuilderImpl(SLAImpl sla, SLABuilderImpl slaBldr) {
            this.slaBuilder = slaBldr;
            this.sla = sla;
        }

        @Override
        public ObligationBuilder description(String descr) {
            this.description = descr;
            return this;
        }

        @Override
        public ObligationBuilder metric(Metric metric) {
            this.metrics.add(metric);
            return this;
        }
        
        @Override
        public Obligation build() {
            ObligationImpl ob = new ObligationImpl();
            ob.description = this.description;
            ob.metrics = this.metrics;
            ob.sla = this.sla;
            return ob;
        }
        
        @Override
        public ServiceLevelAgreementBuilder add() {
            ObligationImpl ob = (ObligationImpl) build();
            this.sla.getObligations().add(ob);
            return this.slaBuilder;
        }
    }
    
    
    private static class SLAID implements ServiceLevelAgreement.ID {
        private final UUID uuid;
        
        public SLAID() {
            this(UUID.randomUUID());
        }
        
        public SLAID(String str) {
            this(UUID.fromString(str));
        }
        
        public SLAID(UUID id) {
            this.uuid = id;
        }
        
        @Override
        public String toString() {
            return this.uuid.toString();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (! this.getClass().equals(obj.getClass()))
                return false;
            
            return Objects.equals(this.uuid, ((SLAID) obj).uuid);
         }
        
        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }
        
    }
    
    private static class SLAImpl implements ServiceLevelAgreement {
        
        private ServiceLevelAgreement.ID id;
        private String name;
        private DateTime creationTime = DateTime.now();
        private String description;
        private Set<Obligation> obligations;
        
        public SLAImpl() {
            this.obligations = new HashSet<Obligation>();
        }

        public ServiceLevelAgreement.ID getId() {
            return id;
        }

        protected void setId(ServiceLevelAgreement.ID id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name;
        }

        protected void setName(String name) {
            this.name = name;
        }
        
        @Override
        public DateTime getCreationTime() {
            return this.creationTime;
        }
        
        public void setCreationTime(DateTime creationTime) {
            this.creationTime = creationTime;
        }

        @Override
        public String getDescription() {
            return description;
        }

        protected void setDescription(String description) {
            this.description = description;
        }

        @Override
        public Set<Obligation> getObligations() {
            return obligations;
        }

        protected void setObligations(Set<Obligation> obligations) {
            this.obligations = obligations;
        }
    }

    private static class ObligationImpl implements Obligation {
        
        private SLAImpl sla;
        private String description;
        private Set<Metric> metrics = new HashSet<Metric>();

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public ServiceLevelAgreement getSLA() {
            return this.sla;
        }

        @Override
        public Set<Metric> getMetrics() {
            return Collections.unmodifiableSet(this.metrics);
        }
        
    }
}
