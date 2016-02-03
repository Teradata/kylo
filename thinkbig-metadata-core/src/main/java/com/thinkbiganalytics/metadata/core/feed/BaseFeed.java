/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedData;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public class BaseFeed implements Feed {

    private ID Id;
    private String Name;
    private String Description;
    private Set<FeedSource> sources = new HashSet<>();
    private FeedDestination destination;
    
    

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

    public Set<FeedSource> getSources() {
        return this.sources;
    }

    public FeedDestination getDestination() {
        return destination;
    }
    
    public FeedSource addSource(Dataset ds) {
        return addSource(ds, null);
    }

    public FeedSource addSource(Dataset ds, ServiceLevelAgreement.ID agreemenetId) {
        Source src = new Source(ds, agreemenetId);
        this.sources.add(src);
        return src;
    }

    public FeedDestination addDestination(Dataset ds) {
        this.destination = new Destination(ds);
        return destination;
    }
    
    private static class FeedId implements ID {
        private UUID uuid = UUID.randomUUID();
        
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

    private abstract class Data implements FeedData {
        
        private Dataset dataset;
        
        public Data(Dataset ds) {
            this.dataset = ds;
        }

        @Override
        public Dataset getDataset() {
            return this.dataset;
        }
    }
    
    private class Source extends Data implements FeedSource {

        private ServiceLevelAgreement.ID agreemenetId;
        
        public Source(Dataset ds, ServiceLevelAgreement.ID agreementId) {
            super(ds);
            this.agreemenetId = agreementId;
        }

        @Override
        public ServiceLevelAgreement.ID getAgreementId() {
            return this.agreemenetId;
        }
    }
    
    private class Destination extends Data implements FeedDestination {

        public Destination(Dataset ds) {
            super(ds);
        }
    }

}
