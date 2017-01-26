/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup.Condition;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="SLA_OBLIGATION_GROUP")
public class JpaObligationGroup extends AbstractAuditedEntity implements ObligationGroup, Serializable {
    
    private static final long serialVersionUID = 3948150775928992180L;

    @Id
    @GeneratedValue
    @Column(name="id", columnDefinition="binary(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name="cond", length=10)
    private Condition condition = Condition.REQUIRED;
    
    @ManyToOne
    private JpaServiceLevelAgreement agreement;
    
    @OneToMany(targetEntity=JpaObligation.class, mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Obligation> obligations = new ArrayList<>();
    
    public JpaObligationGroup() {
    }

    public JpaObligationGroup(JpaServiceLevelAgreement sla, Condition cond) {
        sla.addGroup(this);
        setCondition(cond);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationGroup#getCondition()
     */
    @Override
    public Condition getCondition() {
        return this.condition;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationGroup#getObligations()
     */
    @Override
    public List<Obligation> getObligations() {
        return this.obligations;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ObligationGroup#getAgreement()
     */
    @Override
    public ServiceLevelAgreement getAgreement() {
        return this.agreement;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public void setAgreement(JpaServiceLevelAgreement agreement) {
        this.agreement = agreement;
    }

    
}
