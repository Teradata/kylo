/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.datasource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.FeedConnection;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeedDestination;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeedSource;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="DATASOURCE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type")
//public abstract class JpaDatasource implements Datasource, Serializable {
public class JpaDatasource implements Datasource, Serializable {

    private static final long serialVersionUID = -2805184157648437890L;
    
    @EmbeddedId
    private DatasourceId id;
    
    @Column(name="name", length=100, unique=true)
    private String name;
    
    @Column(name="description", length=255)
    private String description;
    
    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name="created_time")
    private DateTime createdTime;
    
    @OneToMany(targetEntity=JpaFeedSource.class, mappedBy = "datasource", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeedSource> feedSources = new HashSet<>();
    
    @OneToMany(targetEntity=JpaFeedDestination.class, mappedBy = "datasource", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FeedDestination> feedDestinations = new HashSet<>();
    
    @Transient  // TODO implement
    private List<Dataset<? extends Datasource, ? extends ChangeSet>> datasets = new ArrayList<>();
    
    public JpaDatasource() {
    }

    public JpaDatasource(String name, String descr) {
        this.createdTime = new DateTime();
        this.name = name;
        this.description = descr;
    }

    public ID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public List<Dataset<? extends Datasource, ? extends ChangeSet>> getDatasets() {
        return datasets;
    }
    
    public Set<FeedConnection> getFeedConnections() {
        return Sets.newHashSet(Iterables.concat(getFeedSources(), getFeedDestinations()));
    }
    
    public Set<FeedSource> getFeedSources() {
        return feedSources;
    }

    public void setFeedSources(Set<FeedSource> feedSources) {
        this.feedSources = feedSources;
    }

    public Set<FeedDestination> getFeedDestinations() {
        return feedDestinations;
    }

    public void setFeedDestinations(Set<FeedDestination> feedDestinations) {
        this.feedDestinations = feedDestinations;
    }

    public void addFeedSource(JpaFeedSource src) {
        this.feedSources.add(src);
        src.setDatasource(this);
    }

    public void addFeedDestination(JpaFeedDestination dest) {
        this.feedDestinations.add(dest);
        dest.setDatasource(this);
    }




    @Embeddable
    protected static class DatasourceId implements ID {
        
        private static final long serialVersionUID = 241001606640713117L;
        
        //@Column(name="id", columnDefinition="binary(36)")
        @Column(name="id", columnDefinition="binary(16)", length = 16)
        private UUID uuid;
        
        public static DatasourceId create() {
            return new DatasourceId(UUID.randomUUID());
        }
        
        public DatasourceId() {
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
        
        public DatasourceId(Serializable ser) {
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
            if (obj instanceof DatasourceId) {
                DatasourceId that = (DatasourceId) obj;
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
}
