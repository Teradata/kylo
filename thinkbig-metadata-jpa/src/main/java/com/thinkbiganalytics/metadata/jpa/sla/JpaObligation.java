/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="SLA_OBLIGATION")
public class JpaObligation implements Obligation, Serializable {

    private static final long serialVersionUID = -6415493614683081403L;
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne
    private JpaObligationGroup group;
    
    @OneToMany(targetEntity=JpaMetric.class, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Metric> metrics;
    
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
        return this.metrics;
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

    public void setMetrics(Set<Metric> metrics) {
        this.metrics = metrics;
    }
    
}
