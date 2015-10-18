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

import com.thinkbiganalytics.metadata.sla.api.DuplicateAgreementNameException;
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
    private Map<String, ID> nameToSlas;

    /**
     * 
     */
    public InMemorySLAProvider() {
        this.slas = Collections.synchronizedMap(new HashMap<SLAID, ServiceLevelAgreement>());
        this.nameToSlas = Collections.synchronizedMap(new HashMap<String, ID>());
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
    public ServiceLevelAgreement findAgreementByName(String slaName) {
        ID id = this.nameToSlas.get(slaName);
        
        if (id != null) {
            return this.slas.get(id);
        } else {
            return null;
        }
    }

    @Override
    public ServiceLevelAgreement removeAgreement(ID id) {
        synchronized (this.slas) {
            ServiceLevelAgreement sla = this.slas.remove(id);
            
            if (sla != null) {
                this.nameToSlas.remove(sla.getName());
                return sla;
            } else {
                return null;
            }
        }
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
        synchronized (this.slas) {
            SLAID id = new SLAID();
            
            if (this.nameToSlas.containsKey(sla.getName())) {
                throw new DuplicateAgreementNameException(sla.getName());
            } else {
                sla.setId(id);
                this.slas.put(id, sla);
                this.nameToSlas.put(sla.getName(), id);
                return sla;
            }
        }
    }
    
    private SLAImpl replaceSLA(SLAID id, SLAImpl sla) {
        ServiceLevelAgreement oldSla = this.slas.get(id);
        ServiceLevelAgreement.ID namedId = this.nameToSlas.get(sla.getName());
        
        // Make sure the name of the new SLA does not match that of another SLA besides the one being replaced.
        if (namedId == null || oldSla.getId().equals(namedId)) {
            sla.setId(id);
            this.slas.put(id, sla);
            return sla;
        } else {
            throw new DuplicateAgreementNameException(sla.getName());
        }
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
        
        private static final long serialVersionUID = 8914036758972637669L;
        
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
