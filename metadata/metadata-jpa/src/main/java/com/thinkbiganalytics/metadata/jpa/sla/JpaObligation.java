/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="SLA_OBLIGATION")
public class JpaObligation extends AbstractAuditedEntity implements Obligation, Serializable {

    private static final long serialVersionUID = -6415493614683081403L;
    
    @Id
    @GeneratedValue
    @Column(name="id", columnDefinition="binary(16)")
    private UUID id;
    
    @ManyToOne
    private JpaObligationGroup group;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="obligation_id")
    private Set<JpaMetricWrapper> metricWrappers = new HashSet<>();
    
    @Column(name="description", length=255)
    private String description;

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public ServiceLevelAgreement getAgreement() {
        return getGroup().getAgreement();
    }

    @Override
    public ObligationGroup getGroup() {
        return group;
    }

    @Override
    public Set<Metric> getMetrics() {
        return Sets.newHashSet(Iterables.transform(this.metricWrappers, new Function<JpaMetricWrapper, Metric>() {
            @Override
            public Metric apply(JpaMetricWrapper input) {
                return input.getMetric();
            }
        }));
    }

    public void setMetrics(Set<Metric> metrics) {
        for (Metric metric : metrics) {
            this.metricWrappers.add(new JpaMetricWrapper(metric));
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setGroup(JpaObligationGroup group) {
        this.group = group;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<JpaMetricWrapper> getMetricWrappers() {
        return metricWrappers;
    }

    public void setMetricWrappers(Set<JpaMetricWrapper> metricWrappers) {
        this.metricWrappers = metricWrappers;
    }
    
    
}
