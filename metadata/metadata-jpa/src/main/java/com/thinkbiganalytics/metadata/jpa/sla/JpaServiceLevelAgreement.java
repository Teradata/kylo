/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.sla;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="SLA")
public class JpaServiceLevelAgreement implements ServiceLevelAgreement, Serializable {

    private static final long serialVersionUID = 2611479261936214396L;

    @EmbeddedId
    @Column(name="id", columnDefinition="binary(16)")
    private SlaId id;
    
    @Column(name="name", length=100, unique=true)
    private String name;
    
    private String description;
    
    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    @Column(name="created_time")
    private DateTime createdTime;
    
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
     * @see com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement#getCreatedTime()
     */
    @Override
    public DateTime getCreatedTime() {
        return this.createdTime;
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
    public static class SlaId implements ServiceLevelAgreement.ID, Serializable {
        
        private static final long serialVersionUID = 6965221468619613881L;
        
        @Column(name="id", columnDefinition="binary(16)")
        private UUID uuid;
        
        public static SlaId create() {
            return new SlaId(UUID.randomUUID());
        }
        
        public SlaId() {
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
        
        public SlaId(Serializable ser) {
            if (ser instanceof String) {
                this.uuid = UUID.fromString((String) ser);
            } else if (ser instanceof UUID) {
                this.uuid = (UUID) ser;
            } else {
                throw new IllegalArgumentException("Unknown ID value: " + ser);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SlaId) {
                SlaId that = (SlaId) obj;
                return Objects.equals(this.uuid, that.uuid);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(getClass(), this.uuid);
        }
        
        @Override
        public String toString() {
            return this.uuid.toString();
        }
    }
//    
//    
//    @Embeddable
//    public static class SlaId extends BaseId implements ServiceLevelAgreement.ID, Serializable {
//        
//        public SlaId() {
//            super();
//        }
//        
//        public SlaId(Serializable ser) {
//            super(ser);
//        }
//    }
}
