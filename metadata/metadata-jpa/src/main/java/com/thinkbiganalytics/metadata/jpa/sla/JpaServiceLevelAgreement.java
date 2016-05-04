/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.jpa.BaseId;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="SLA")
public class JpaServiceLevelAgreement extends AbstractAuditedEntity implements ServiceLevelAgreement, Serializable {

    private static final long serialVersionUID = 2611479261936214396L;

    @EmbeddedId
    private SlaId id;
    
    @Column(name="name", length=100, unique=true)
    private String name;
    
    @Column(name="description", length=255)
    private String description;
    
    @OneToMany(targetEntity=JpaObligationGroup.class, mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObligationGroup> obligationGroups;
    
    /**
     * 
     */
    public JpaServiceLevelAgreement() {
    }

    public JpaServiceLevelAgreement(String name, String description) {
        super();
        this.id = SlaId.create();
        this.name = name;
        this.description = description;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getId()
     */
    @Override
    public ID getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getObligationGroups()
     */
    @Override
    public List<ObligationGroup> getObligationGroups() {
        return this.obligationGroups;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getObligations()
     */
    @Override
    public List<Obligation> getObligations() {
        List<Obligation> list = new ArrayList<>();
        
        for (ObligationGroup group : getObligationGroups()) {
            list.addAll(group.getObligations());
        }
        
        return list;
    }

    
    @Embeddable
    public static class SlaId extends BaseId implements ServiceLevelAgreement.ID, Serializable {
        
        private static final long serialVersionUID = 6965221468619613881L;
        
        @Column(name="id", columnDefinition="binary(16)", length = 16)
        private UUID uuid;
        
        public static SlaId create() {
            return new SlaId(UUID.randomUUID());
        }
        
        public SlaId() {
        }
        
        public SlaId(Serializable ser) {
            super(ser);
        }
        
        @Override
        public UUID getUuid() {
            return this.uuid;
        }
        
        @Override
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
    }
}
