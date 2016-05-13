/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.persistence.*;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.jpa.NamedJpaQueries;
import com.thinkbiganalytics.metadata.jpa.category.JpaCategory;
import com.thinkbiganalytics.metadata.jpa.feedmgr.FeedManagerNamedQueries;
import com.thinkbiganalytics.metadata.jpa.feedmgr.category.JpaFeedManagerCategory;
import org.hibernate.annotations.GenericGenerator;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.jpa.AuditTimestampListener;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.jpa.BaseId;
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
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners(AuditTimestampListener.class)
@NamedQuery(
        name = NamedJpaQueries.FEED_FIND_BY_SYSTEM_NAME,
        query = "select feed FROM JpaFeed as feed INNER JOIN FETCH feed.category as c WHERE feed.name = :systemName"
)
public class JpaFeed<C extends Category> extends AbstractAuditedEntity implements Feed<C>{

    private static final long serialVersionUID = 404021578157775507L;

    @EmbeddedId
    private FeedId Id;
    
    @Column(name="name", length=100, unique=true, nullable=false)
    private String name;
    
    @Column(name="display_name", length=100, unique=true)
    private String displayName;

    @Column(name="description", length=255)
    private String description;
    
    @Column(name="initialized", length=1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean initialized;
    
    @Enumerated(EnumType.STRING)
    @Column(name="state", length=10, nullable=false)
    private State state = State.ENABLED;
    
    @OneToMany(targetEntity=JpaFeedSource.class, mappedBy = "feed", fetch=FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedSource> sources = new ArrayList<>();
    
    @OneToMany(targetEntity=JpaFeedDestination.class, mappedBy = "feed", fetch=FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedDestination> destinations = new ArrayList<>();
    
    @ElementCollection
    @MapKeyColumn(name="prop_key", length=100)
    @Column(name="prop_value")
    @CollectionTable(name="FEED_PROPERTIES")
    private Map<String, String> properties = new HashMap<>();
    
    @Embedded
    private JpaFeedPrecondition precondition;

    @ManyToOne(targetEntity = JpaCategory.class)
    @JoinColumn(name = "category_id", nullable = false, insertable = true, updatable = false)
    private C category;


    @Version
    @Column(name = "VERSION")
    private Integer version = 1;

    public JpaFeed() {
    }
    public JpaFeed(FeedId feedId) {
        this.Id = feedId;
    }
    
    public JpaFeed(String name, String description) {

        this.Id = FeedId.create();
        this.name = name;
        this.description = description;
    }

    @Override
    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public void setProperties(Map<String, String> props) {
        this.properties.clear();
        for (Entry<String, String> entry : props.entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Map<String, String> mergeProperties(Map<String, String> props) {
        for (Entry<String, String> entry : props.entrySet()) {
            this.properties.put(entry.getKey(), entry.getValue());
        }
        return this.properties;
    }

    @Override
    public String setProperty(String key, String value) {
        return this.properties.put(key, value);
    }

    @Override
    public String removeProperty(String key) {
        return this.properties.remove(key);
    }

    public ID getId() {
        return Id;
    }
    
    public void setId(FeedId id) {
        Id = id;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        if (this.displayName != null) {
            return this.displayName;
        } else {
            return getName();
        }
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrecondition(JpaFeedPrecondition precondition) {
        this.precondition = precondition;
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
    
    public State getState() {
        return state;
    }
    
    public void setState(State state) {
        this.state = state;
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


    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public C getCategory() {
        return category;
    }


    public  void setCategory( C category) {
        this.category = category;
    }

    @Embeddable
    public static class FeedId extends BaseId implements Feed.ID {
        
        private static final long serialVersionUID = -8322308917629324338L;
      
        @GeneratedValue(generator = "uuid")
        @GenericGenerator(name = "uuid", strategy = "uuid")
        @Column(name="id", columnDefinition="binary(16)")
        private UUID uuid;
        
        public static FeedId create() {
            return new FeedId(UUID.randomUUID());
        }
        
        public FeedId() {
        }
        
        public FeedId(Serializable ser) {
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

    
    @Embeddable
    public static class JpaFeedPrecondition implements FeedPrecondition {
        
        @OneToOne(fetch=FetchType.EAGER)
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
