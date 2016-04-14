/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.op;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasource;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed.FeedId;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeedDestination;

/**
 *
 * @author Sean Felten
 */
@Entity
@Table(name="DATA_OPERATION")
public class JpaDataOperation implements DataOperation {

    private static final long serialVersionUID = 4869637300972732879L;

    @EmbeddedId
    private OpId id;
    
    @ManyToOne
    private JpaFeedDestination producer;
    
    @OneToOne
    private JpaDataset<Datasource, ChangeSet> dataset;
    
    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime startTime;

    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime stopTime;

    private State state;

    private String status = "";

    public void setId(OpId id) {
        this.id = id;
    }

    public void setProducer(JpaFeedDestination producer) {
        this.producer = producer;
    }

    public void setDataset(JpaDataset<Datasource, ChangeSet> dataset) {
        this.dataset = dataset;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public void setStopTime(DateTime stopTime) {
        this.stopTime = stopTime;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public JpaDataOperation() {
    }

    public JpaDataOperation(JpaDatasource ds, JpaFeedDestination feedDest) {
        this(ds, feedDest, "Operation in progress", new DateTime());
    }
    
    public JpaDataOperation(JpaDatasource ds, JpaFeedDestination feedDest, DateTime time) {
        this(ds, feedDest, "Operation in progress", time);
    }
    
    public JpaDataOperation(JpaDatasource ds, JpaFeedDestination feedDest, String status, DateTime time) {
        this.id = OpId.create();
        this.state = State.IN_PROGRESS;
        this.producer = feedDest;
        this.status = status;
        this.startTime = time;
        // TODO change relationship to direct ref to op from dataset
    }

    public JpaDataOperation(JpaDataOperation op, State state, String status) {
        this.id = op.id;
        this.startTime = op.startTime;
        this.producer = op.producer;
        
        this.state = state;
        this.status = StringUtils.isEmpty(status) && state != State.IN_PROGRESS 
                ? "Operation completed with result: " + state.toString() 
                : status;
        this.stopTime = state != State.IN_PROGRESS ? new DateTime() : op.stopTime;
    }

    public JpaDataOperation(JpaDataOperation op, String status, JpaDataset<Datasource, ChangeSet> changes) {
        this(op, State.SUCCESS, "Operation completed successfully");

        this.dataset = changes;
    }
    
    public JpaDataOperation(JpaDataOperation op, String status, Throwable t) {
        this(op, State.FAILURE, "Operation failed: " + t.getMessage());
    }

    @Override
    public ID getId() {
        return id;
    }

    @Override
    public DateTime getStartTime() {
        return this.startTime;
    }

    @Override
    public DateTime getStopTime() {
        return this.stopTime;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public FeedDestination getProducer() {
        return producer;
    }

    @Override
    public Dataset<Datasource, ChangeSet> getDataset() {
        return dataset;
    }
    
    
    protected static class OpId implements ID {
        
        private static final long serialVersionUID = -8322308917629324338L;

        @Column(name="id")
        private UUID uuid;
        
        public static OpId create() {
            return new OpId(UUID.randomUUID());
        }
        
        public OpId() {
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
        
        public OpId(Serializable ser) {
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
                OpId that = (OpId) obj;
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
