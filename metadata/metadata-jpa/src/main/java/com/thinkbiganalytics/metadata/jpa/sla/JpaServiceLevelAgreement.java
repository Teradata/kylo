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
import javax.persistence.Transient;

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
    private List<ObligationGroup> obligationGroups = new ArrayList<>();
    
    @Transient
    private JpaObligationGroup defaultGroup;
    
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
        this.defaultGroup = new JpaObligationGroup();
        
        addGroup(this.defaultGroup);
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
    
    public JpaObligationGroup getDefaultGroup() {
        return defaultGroup;
    }

    public void addGroup(JpaObligationGroup group) {
        getObligationGroups().add(group);
        group.setAgreement(this);
    }

    public void setId(SlaId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setObligationGroups(List<ObligationGroup> obligationGroups) {
        this.obligationGroups = obligationGroups;
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
