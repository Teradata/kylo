/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;
import com.thinkbiganalytics.metadata.jpa.sla.JpaServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="FEED")
public class JpaFeed implements Feed {

    private static final long serialVersionUID = 404021578157775507L;

    @EmbeddedId
    private FeedId Id;
    
    @Column(name="name", length=100, unique=true)
    private String name;

    private String description;
    
    @OneToMany(targetEntity=JpaFeedSource.class, mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedSource> sources = new ArrayList<>();
    
    @OneToMany(targetEntity=JpaFeedDestination.class, mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedDestination> destinations = new ArrayList<>();
    
    @Embedded
    private JpaFeedPrecondition precondition;
    

    public JpaFeed() {
    }
    
    public JpaFeed(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ID getId() {
        return Id;
    }
    
    public void setId(FeedId id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<FeedSource> getSources() {
        return this.sources;
    }

    public List<FeedDestination> getDestinations() {
        return this.destinations;
    }
    
    @Override
    public FeedDestination getDestination(Datasource.ID id) {
        // TODO is there a sexy JPA/Hibernate way to do this since there is an implicit session used to get the destinations?
        for (FeedDestination dest : getDestinations()) {
            if (dest.getDatasource().getId().equals(id)) {
                return dest;
            }
        }
        
        return null;
    }
    
    @Override
    public FeedDestination getDestination(FeedDestination.ID id) {
        // TODO is there a sexy JPA/Hibernate way to do this since there is an implicit session used to get the destinations?
        for (FeedDestination dest : getDestinations()) {
            if (dest.getId().equals(id)) {
                return dest;
            }
        }
        
        return null;
    }

    public JpaFeedDestination addDestination(Datasource ds) {
        JpaFeedDestination dest = new JpaFeedDestination(this, (JpaDatasource) ds);
        getDestinations().add(dest);
        dest.setFeed(this);
        return dest;
    }

    @Override
    public FeedPrecondition getPrecondition() {
        return this.precondition;
    }

    public JpaFeedSource addSource(Datasource ds) {
        return addSource((JpaDatasource) ds, null);
    }

    public JpaFeedSource addSource(JpaDatasource ds, JpaServiceLevelAgreement agreement) {
        JpaFeedSource src = new JpaFeedSource(this, ds, agreement);
        getSources().add(src);
        src.setFeed(this);
        return src;
    }
    
    @Override
    public FeedSource getSource(Datasource.ID id) {        
        // TODO is there a sexy JPA/Hibernate way to do this since there is an implicit session used to get the sources?
        for (FeedSource dest : getSources()) {
            if (dest.getDatasource().getId().equals(id)) {
                return dest;
            }
        }
        
        return null;
    }
    
    @Override
    public FeedSource getSource(FeedSource.ID id) {        
        // TODO is there a sexy JPA/Hibernate way to do this since there is an implicit session used to get the sources?
        for (FeedSource src : getSources()) {
            if (src.getId().equals(id)) {
                return src;
            }
        }
        
        return null;
    }
    
    public FeedPrecondition setPrecondition(JpaServiceLevelAgreement sla) {
        this.precondition = new JpaFeedPrecondition(sla);
        return this.precondition;
    }

    
    @Embeddable
    public static class FeedId implements Feed.ID {
        
        private static final long serialVersionUID = -8322308917629324338L;

        //@Column(name="id", columnDefinition="binary(36)")
        @Column(name="id", columnDefinition="binary(16)", length = 16)
        private UUID uuid;
        
        public static FeedId create() {
            return new FeedId(UUID.randomUUID());
        }
        
        public FeedId() {
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
        
        public FeedId(Serializable ser) {
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
            if (obj instanceof FeedId) {
                FeedId that = (FeedId) obj;
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
//    public static class FeedId extends BaseId implements Feed.ID {
//        
//        private static final long serialVersionUID = -8322308917629324338L;
//        
//        public FeedId() {
//            super();
//        }
//        
//        public FeedId(Serializable ser) {
//            super(ser);
//        }
//    }

    
    @Embeddable
    public static class JpaFeedPrecondition implements FeedPrecondition {
        
        @OneToOne
        private JpaServiceLevelAgreement sla;
        
        public JpaFeedPrecondition() {
        }
        
        public JpaFeedPrecondition(JpaServiceLevelAgreement sla) {
            this.sla = sla;
        }

        @Override
        public String getName() {
            return this.sla.getName();
        }
        
        @Override
        public String getDescription() {
            return this.sla.getDescription();
        }

        @Override
        public Set<Metric> getMetrics() {
            Set<Metric> set = new HashSet<>();
            for (Obligation ob : this.sla.getObligations()) {
                set.addAll(ob.getMetrics());
            }
            return set;
        }
        
        protected ServiceLevelAgreement getAgreement() {
            return sla;
        }
    }

}
