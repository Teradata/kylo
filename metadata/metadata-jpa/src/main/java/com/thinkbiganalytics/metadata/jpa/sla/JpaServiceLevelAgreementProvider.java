/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import com.thinkbiganalytics.metadata.jpa.sla.JpaServiceLevelAgreement.SlaId;
import com.thinkbiganalytics.metadata.sla.api.AgreementNotFoundException;
import com.thinkbiganalytics.metadata.sla.api.DuplicateAgreementNameException;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement.ID;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfiguration;
import com.thinkbiganalytics.metadata.sla.spi.ObligationBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ObligationGroupBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementBuilder;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

import org.springframework.beans.factory.annotation.Qualifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Sean Felten
 */
public class JpaServiceLevelAgreementProvider implements ServiceLevelAgreementProvider {

    @Inject
    @Qualifier("metadataEntityManager")
    private EntityManager entityMgr;

    @Override
    public ID resolve(Serializable id) {
        if (id instanceof JpaServiceLevelAgreement.SlaId) {
            return (JpaServiceLevelAgreement.SlaId) id;
        } else {
            return new JpaServiceLevelAgreement.SlaId(id);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceLevelAgreement> getAgreements() {
        return new ArrayList<ServiceLevelAgreement>(this.entityMgr.createQuery("select f from JpaServiceLevelAgreement f").getResultList());
    }

    @Override
    public ServiceLevelAgreement getAgreement(ID id) {
        return this.entityMgr.find(JpaServiceLevelAgreement.class, id); 
    }

    @Override
    public ServiceLevelAgreement findAgreementByName(String slaName) {
        Query query = this.entityMgr.createQuery("select s from JpaServiceLevelAgreement s where s.name = :slaName", JpaServiceLevelAgreement.class);
        query.setParameter("slaName", slaName);
        @SuppressWarnings("unchecked")
        List<JpaServiceLevelAgreement> list = query.getResultList();
        
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public boolean removeAgreement(ID id) {
        JpaServiceLevelAgreement sla = this.entityMgr.find(JpaServiceLevelAgreement.class, id);
        
        if (sla != null) {
            this.entityMgr.remove(sla);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ServiceLevelAgreementBuilder builder() {
        return new SLABuilderImpl();
    }

    @Override
    public ServiceLevelAgreementBuilder builder(ID id) {
        SlaId slaId = (SlaId) id;
        return new SLABuilderImpl(slaId);
    }

    private ServiceLevelAgreement createAgreement(JpaServiceLevelAgreement sla) {
        JpaServiceLevelAgreement existing = (JpaServiceLevelAgreement) findAgreementByName(sla.getName());
        
        if (existing == null) {
            sla.setId(SlaId.create());
            this.entityMgr.persist(sla);
            return sla;
        } else {
            throw new DuplicateAgreementNameException(sla.getName());
        }
    }

    private ServiceLevelAgreement replaceAgreement(SlaId id, JpaServiceLevelAgreement update) {
        JpaServiceLevelAgreement existing = this.entityMgr.find(JpaServiceLevelAgreement.class, id);
        
        if (existing != null) {
            update.setId(id);
            this.entityMgr.merge(update);
            return update;
        } else {
            throw new AgreementNotFoundException(update.getId());
        }
    }




    private class SLABuilderImpl implements ServiceLevelAgreementBuilder {

        private JpaServiceLevelAgreement.SlaId id;
        private String name;
        private String descrtion;
        private JpaServiceLevelAgreement sla = new JpaServiceLevelAgreement();

        public SLABuilderImpl() {
            this(null);
        }

        public SLABuilderImpl(JpaServiceLevelAgreement.SlaId id) {
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
        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder() {
            return new ObligationBuilderImpl<ServiceLevelAgreementBuilder>(this.sla.getDefaultGroup(), this);
        }
        
        @Override
        public ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder(Condition condition) {
            JpaObligationGroup group = new JpaObligationGroup(this.sla, condition);
            this.sla.getObligationGroups().add(group);
            return new ObligationBuilderImpl<ServiceLevelAgreementBuilder>(group, this);
        }

        @Override
        public ObligationGroupBuilder obligationGroupBuilder(Condition condition) {
            return new ObligationGroupBuilderImpl(this, condition);
        }

        @Override
        public ServiceLevelAgreement build() {
            this.sla.setName(this.name);
            this.sla.setDescription(this.descrtion);

            if (this.id == null) {
                return createAgreement(sla);
            } else {
                return replaceAgreement(this.id, sla);
            }
        }

        @Override
        public ServiceLevelAgreementBuilder actionConfigurations(List<? extends ServiceLevelAgreementActionConfiguration> actionConfigurations) {
            return null;
        }
    }

    private static class ObligationBuilderImpl<B> implements ObligationBuilder<B> {

        private SLABuilderImpl slaBuilder;
        private ObligationGroupBuilderImpl groupBuilder;
        private JpaObligationGroup group;
        private String description;
        private Set<Metric> metrics = new HashSet<Metric>();

        public ObligationBuilderImpl(JpaObligationGroup group, SLABuilderImpl bldr) {
            this.slaBuilder = bldr;
            this.group = group;
        }
        
        public ObligationBuilderImpl(JpaObligationGroup group, ObligationGroupBuilderImpl bldr) {
            this.groupBuilder = bldr;
            this.group = group;
        }

        @Override
        public ObligationBuilder<B> description(String descr) {
            this.description = descr;
            return this;
        }

        @Override
        public ObligationBuilder<B> metric(Metric metric, Metric... more) {
            this.metrics.add(metric);
            for (Metric another : more) {
                this.metrics.add(another);
            }
            return this;
        }

        @Override
        public ObligationBuilder<B> metric(Collection<Metric> metrics) {
            this.metrics.addAll(metrics);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public B build() {
            JpaObligation ob = new JpaObligation();
            ob.setDescription(this.description);
            ob.setMetrics(this.metrics);
            ob.setGroup(this.group);
            this.group.getObligations().add(ob);
            
            if (this.groupBuilder != null) {
                return (B) this.groupBuilder;
            } else {
                return (B) this.slaBuilder;
            }
        }
    }

    private static class ObligationGroupBuilderImpl implements ObligationGroupBuilder {

        private SLABuilderImpl slaBuilder;
        private JpaObligationGroup group;
        
        public ObligationGroupBuilderImpl(SLABuilderImpl slaBuilder, Condition cond) {
            this.slaBuilder = slaBuilder;
            this.group = new JpaObligationGroup(this.slaBuilder.sla, cond);
        }

        @Override
        public ObligationGroupBuilder obligation(Obligation obligation) {
            this.group.getObligations().add(obligation);
            return this;
        }

        @Override
        public ObligationBuilder<ObligationGroupBuilder> obligationBuilder() {
            return new ObligationBuilderImpl<ObligationGroupBuilder>(this.group, this);
        }

        @Override
        public ServiceLevelAgreementBuilder build() {
            this.slaBuilder.sla.getObligationGroups().add(this.group);
            return this.slaBuilder;
        }
    }

}
