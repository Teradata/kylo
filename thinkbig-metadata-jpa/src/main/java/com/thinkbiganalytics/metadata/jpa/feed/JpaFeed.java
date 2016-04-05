/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
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
    
    private String Name;
    private String Description;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedSource> sources = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedDestination> destinations = new ArrayList<>();
    
    @Embedded
    private FeedPreconditionImpl precondition;
    

    public JpaFeed() {
    }
    
    public JpaFeed(String name, String description) {
        this.Id = new FeedId();
        Name = name;
        Description = description;
    }

    public ID getId() {
        return Id;
    }
    
    public void setId(FeedId id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
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

    public FeedDestination addDestination(Datasource ds) {
        JpaFeedDestination dest = new JpaFeedDestination(this, (JpaDatasource) ds);
        getDestinations().add(dest);
        return dest;
    }

    @Override
    public FeedPrecondition getPrecondition() {
        return this.precondition;
    }

    public FeedSource addSource(Datasource ds) {
        return addSource((JpaDatasource) ds, null);
    }

    public FeedSource addSource(JpaDatasource ds, JpaServiceLevelAgreement agreemenet) {
        JpaFeedSource src = new JpaFeedSource(this, ds, agreemenet);
        getSources().add(src);
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
    
    public FeedPrecondition setPrecondition(ServiceLevelAgreement sla) {
        this.precondition = new FeedPreconditionImpl(sla);
        return this.precondition;
    }
    
    @Embeddable
    public static class FeedId extends BaseId implements Feed.ID {
        
        private static final long serialVersionUID = -8322308917629324338L;
    
        public FeedId() {
            super();
        }
    
        public FeedId(Serializable ser) {
            super(ser);
        }
    }
//
//    public static abstract class BaseId {
//        private final UUID uuid;
//        
//        public BaseId() {
//            this.uuid = UUID.randomUUID();
//        }
//        
//        public BaseId(Serializable ser) {
//            if (ser instanceof String) {
//                this.uuid = UUID.fromString((String) ser);
//            } else if (ser instanceof UUID) {
//                this.uuid = (UUID) ser;
//            } else {
//                throw new IllegalArgumentException("Unknown ID value: " + ser);
//            }
//        }
//        
//        @Override
//        public boolean equals(Object obj) {
//            if (getClass().isAssignableFrom(obj.getClass())) {
//                BaseId that = (BaseId) obj;
//                return Objects.equals(this.uuid, that.uuid);
//            } else {
//                return false;
//            }
//        }
//        
//        @Override
//        public int hashCode() {
//            return Objects.hash(getClass(), this.uuid);
//        }
//        
//        @Override
//        public String toString() {
//            return this.uuid.toString();
//        }
//    }
//    
//    public static class SourceId extends BaseId implements FeedSource.ID {
//        public SourceId() {
//            super();
//        }
//
//        public SourceId(Serializable ser) {
//            super(ser);
//        } 
//    }
//    
//    public static class DestinationId extends BaseId implements FeedDestination.ID {
//        public DestinationId() {
//            super();
//        }
//
//        public DestinationId(Serializable ser) {
//            super(ser);
//        } 
//    }
//    
//
//    private abstract class Data implements FeedData {
//        
//        private Datasource dataset;
//        
//        public Data(Datasource ds) {
//            this.dataset = ds;
//        }
//        
//        @Override
//        public Feed getFeed() {
//            return JpaFeed.this;
//        }
//
//        @Override
//        public Datasource getDatasource() {
//            return this.dataset;
//        }
//    }
//    
//    private class Source extends Data implements FeedSource {
//
//        private SourceId id;
//        private ServiceLevelAgreement.ID agreemenetId;
//        
//        public Source(Datasource ds, ServiceLevelAgreement.ID agreementId) {
//            super(ds);
//            this.id = new SourceId();
//            this.agreemenetId = agreementId;
//        }
// 
//        @Override
//        public ID getId() {
//            return this.id;
//        }
//        
//        @Override
//        public ServiceLevelAgreement getAgreement() {
//            return this.agreemenetId;
//        }
//    }
//    
//    private class Destination extends Data implements FeedDestination {
//
//        private DestinationId id;
//        
//        public Destination(Datasource ds) {
//            super(ds);
//            this.id = new DestinationId();
//        }
//        
//        @Override
//        public ID getId() {
//            return this.id;
//        }
//    }
    
    @Embeddable
    public static class FeedPreconditionImpl implements FeedPrecondition {
        
        @OneToOne
        private ServiceLevelAgreement sla;
        
        public FeedPreconditionImpl(ServiceLevelAgreement sla) {
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
