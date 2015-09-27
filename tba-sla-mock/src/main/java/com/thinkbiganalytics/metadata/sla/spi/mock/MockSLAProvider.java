/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.SLA;
import com.thinkbiganalytics.metadata.sla.api.SLA.ID;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ObligationAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ObligationBuilder;
import com.thinkbiganalytics.metadata.sla.spi.SLABuilder;
import com.thinkbiganalytics.metadata.sla.spi.SLAProvider;

/**
 *
 * @author Sean Felten
 */
public class MockSLAProvider implements SLAProvider {
    
    private Set<ObligationAssessor<Obligation>> obligationAssessors;
    private Set<MetricAssessor<Metric>> metricAssessors;
    private Map<SLA.ID, SLA> slas;

    /**
     * 
     */
    public MockSLAProvider() {
        this.slas = Collections.synchronizedMap(new HashMap<SLA.ID, SLA>());
        this.obligationAssessors = Collections.synchronizedSet(new HashSet<ObligationAssessor<Obligation>>());
        this.metricAssessors = Collections.synchronizedSet(new HashSet<MetricAssessor<Metric>>());
    }
    
    @Override
    public ID resolve(Serializable ser) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SLA> getSLAs() {
        synchronized (this.slas) {
            return new ArrayList<SLA>(this.slas.values());
        }
    }

    @Override
    public SLA getSLA(ID id) {
        return this.slas.get(id);
    }

    @Override
    public SLA remove(ID id) {
        return this.slas.remove(id);
    }
    
    @Override
    public SLABuilder builder() {
        return new MockSLABuilder();
    }
    
    
    private MockSLA addSLA(MockSLA sla) {
        MockSLAID id = new MockSLAID();
        sla.setId(id);
        this.slas.put(id, sla);
        return sla;
    }
    
    
    
    private class MockSLABuilder implements SLABuilder {
        
        private String name;
        private String descrtion;
        private Set<Obligation> obligations = new HashSet<Obligation>();
        private MockSLA sla = new MockSLA();
        
        public MockSLABuilder() {
        }

        @Override
        public SLABuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public SLABuilder description(String description) {
            this.descrtion = description;
            return this;
        }

        @Override
        public SLABuilder obligation(Obligation obligation) {
            this.obligations.add(obligation);
            return this;
        }

        @Override
        public ObligationBuilder obligationBuilder() {
            return new MockObligationBuilder(this.sla, this);
        }
        
        @Override
        public SLA build() {
            this.sla.setName(this.name);
            this.sla.setDescription(this.descrtion);
            this.sla.getObligations().addAll(this.obligations);
            return sla;
        }
    }

    private class MockObligationBuilder implements ObligationBuilder {
        
        private MockSLABuilder slaBuilder;
        private MockSLA sla;
        private String description;
        private Set<Metric> metrics = new HashSet<Metric>();
        
        public MockObligationBuilder(MockSLA sla, MockSLABuilder slaBldr) {
            this.slaBuilder = slaBldr;
            this.sla = sla;
        }

        @Override
        public ObligationBuilder description(String descr) {
            this.description = descr;
            return null;
        }

        @Override
        public ObligationBuilder metric(Metric metric) {
            this.metrics.add(metric);
            return null;
        }
        
        @Override
        public Obligation build() {
            MockObligation ob = new MockObligation();
            ob.description = this.description;
            ob.metrics = this.metrics;
            ob.sla = this.sla;
            this.sla.obligations.add(ob);
            return ob;
        }
        
        @Override
        public SLABuilder add() {
            MockObligation ob = (MockObligation) build();
            this.sla.obligations.add(ob);
            return this.slaBuilder;
        }
    }
    
    
    private static class MockSLAID implements SLA.ID {
        private final UUID uuid = UUID.randomUUID();
    }
    
    private static class MockSLA implements SLA {
        
        private SLA.ID id;
        private String name;
        private String description;
        private Set<Obligation> obligations;
        
        public MockSLA() {
            this.obligations = new HashSet<Obligation>();
        }

        public SLA.ID getId() {
            return id;
        }

        public void setId(SLA.ID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Set<Obligation> getObligations() {
            return obligations;
        }

        public void setObligations(Set<Obligation> obligations) {
            this.obligations = obligations;
        }
    }

    private static class MockObligation implements Obligation {
        
        private MockSLA sla;
        private String description;
        private Set<Metric> metrics = new HashSet<Metric>();

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public SLA getSLA() {
            return this.sla;
        }

        @Override
        public Set<Metric> getMetrics() {
            return Collections.unmodifiableSet(this.metrics);
        }
        
    }
}
