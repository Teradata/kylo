/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import java.util.Objects;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.api.op.DataOperation;

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
    private ChangeSet<Dataset, ChangedContent> changeSet;

    public BaseDataOperation(Dataset ds, FeedDestination feedDest) {
        this(ds, feedDest, "Operation in progress", new DateTime());
    }
    
    public BaseDataOperation(Dataset ds, FeedDestination feedDest, DateTime time) {
        this(ds, feedDest, "Operation in progress", time);
    }
    
    public BaseDataOperation(Dataset ds, FeedDestination feedDest, String status, DateTime time) {
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

    public BaseDataOperation(BaseDataOperation op, String status, ChangeSet<Dataset, ChangedContent> changes) {
        this(op, State.SUCCESS, "Operation completed successfully");

        this.changeSet = changes;
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
    public ChangeSet<Dataset, ChangedContent> getChangeSet() {
        return changeSet;
    }
    
    
    protected static class OpId implements ID {
        private final UUID uuid;
        
        public OpId() {
            this.uuid = UUID.randomUUID();
        }
        
        public OpId(String idStr) {
            this.uuid = UUID.fromString(idStr);
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
