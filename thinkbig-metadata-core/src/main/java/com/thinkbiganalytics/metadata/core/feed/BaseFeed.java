/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedData;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public class BaseFeed implements Feed {

    private ID Id;
    private String Name;
    private String Description;
    private Map<FeedSource.ID, FeedSource> sources = new HashMap<>();
    private Map<FeedDestination.ID, FeedDestination> destinations = new HashMap<>();
    private FeedPreconditionImpl precondition;
    

    public BaseFeed(String name, String description) {
        this.Id = new FeedId();
        Name = name;
        Description = description;
    }

    public ID getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public List<FeedSource> getSources() {
        return new ArrayList<>(this.sources.values());
    }

    public List<FeedDestination> getDestinations() {
        return new ArrayList<>(destinations.values());
    }
    
    @Override
    public FeedDestination getDestination(Datasource.ID id) {
        for (FeedDestination dest : this.destinations.values()) {
            if (dest.getDatasource().getId().equals(id)) {
                return dest;
            }
        }
        
        return null;
    }
    
    @Override
    public FeedPrecondition getPrecondition() {
        return this.precondition;
    }

    public FeedSource addSource(Datasource ds) {
        return addSource(ds, null);
    }

    public FeedSource addSource(Datasource ds, ServiceLevelAgreement agreement) {
        Source src = new Source(ds, agreement);
        this.sources.put(src.getId(), src);
        return src;
    }
    
    @Override
    public FeedSource getSource(Datasource.ID id) {
        for (FeedSource src : this.sources.values()) {
            if (src.getFeed().getId().equals(id)) {
                return src;
            }
        }
        
        return null;
    }

    @Override
    public FeedSource getSource(FeedSource.ID id) {
        return this.sources.get(id);
    }

    public FeedDestination addDestination(Datasource ds) {
        FeedDestination dest = new Destination(ds);
        this.destinations.put(dest.getId(), dest);
        return dest;
    }
    
    @Override
    public FeedDestination getDestination(FeedDestination.ID id) {
        return this.destinations.get(id);
    }
    
    public FeedPrecondition setPrecondition(ServiceLevelAgreement sla) {
        this.precondition = new FeedPreconditionImpl(sla);
        return this.precondition;
    }
    
    private static class BaseId {
        private final UUID uuid;
        
        public BaseId() {
            this.uuid = UUID.randomUUID();
        }
        
        public BaseId(Serializable ser) {
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
            if (getClass().isAssignableFrom(obj.getClass())) {
                BaseId that = (BaseId) obj;
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
    
    
    protected static class FeedId extends BaseId implements Feed.ID {
        public FeedId() {
            super();
        }

        public FeedId(Serializable ser) {
            super(ser);
        }
    }
    
    protected static class SourceId extends BaseId implements FeedSource.ID {
        public SourceId() {
            super();
        }

        public SourceId(Serializable ser) {
            super(ser);
        } 
    }
    
    protected static class DestinationId extends BaseId implements FeedDestination.ID {
        public DestinationId() {
            super();
        }

        public DestinationId(Serializable ser) {
            super(ser);
        } 
    }
    

    private abstract class Data implements FeedData {
        
        private Datasource dataset;
        
        public Data(Datasource ds) {
            this.dataset = ds;
        }
        
        @Override
        public Feed getFeed() {
            return BaseFeed.this;
        }

        @Override
        public Datasource getDatasource() {
            return this.dataset;
        }
    }
    
    private class Source extends Data implements FeedSource {

        private SourceId id;
        private ServiceLevelAgreement agreemenet;
        
        public Source(Datasource ds, ServiceLevelAgreement agreement) {
            super(ds);
            this.id = new SourceId();
            this.agreemenet = agreement;
        }
 
        @Override
        public ID getId() {
            return this.id;
        }
        
        @Override
        public ServiceLevelAgreement getAgreement() {
            return this.agreemenet;
        }
    }
    
    private class Destination extends Data implements FeedDestination {

        private DestinationId id;
        
        public Destination(Datasource ds) {
            super(ds);
            this.id = new DestinationId();
        }
        
        @Override
        public ID getId() {
            return this.id;
        }
    }
    
    protected static class FeedPreconditionImpl implements FeedPrecondition {
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
