/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.Dataset;

/**
 *
 * @author Sean Felten
 */
public class BaseDataOperation implements DataOperation {

    private ID id;
    private DateTime startTime;
    private DateTime stopTime;
    private State state;
    private String status = "";
    private FeedDestination producer;
    private Dataset<Datasource, ChangeSet> dataset;

    public BaseDataOperation(Datasource ds, FeedDestination feedDest) {
        this(ds, feedDest, "Operation in progress", new DateTime());
    }
    
    public BaseDataOperation(Datasource ds, FeedDestination feedDest, DateTime time) {
        this(ds, feedDest, "Operation in progress", time);
    }
    
    public BaseDataOperation(Datasource ds, FeedDestination feedDest, String status, DateTime time) {
        this.id = new OpId();
        this.state = State.IN_PROGRESS;
        this.producer = feedDest;
        this.status = status;
        this.startTime = time;
        // TODO change relationship to direct ref to op from dataset
    }

    public BaseDataOperation(BaseDataOperation op, State state, String status) {
        this.id = op.id;
        this.startTime = op.startTime;
        this.producer = op.producer;
        
        this.state = state;
        this.status = StringUtils.isEmpty(status) && state != State.IN_PROGRESS 
                ? "Operation completed with result: " + state.toString() 
                : status;
        this.stopTime = state != State.IN_PROGRESS ? new DateTime() : op.stopTime;
    }

    public BaseDataOperation(BaseDataOperation op, String status, Dataset<Datasource, ChangeSet> changes) {
        this(op, State.SUCCESS, "Operation completed successfully");

        this.dataset = changes;
    }
    
    public BaseDataOperation(BaseDataOperation op, String status, Throwable t) {
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
        private final UUID uuid;
        
        public OpId() {
            this.uuid = UUID.randomUUID();
        }
        
        public OpId(Serializable ser) {
            this.uuid = UUID.fromString(ser.toString());
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OpId) {
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
