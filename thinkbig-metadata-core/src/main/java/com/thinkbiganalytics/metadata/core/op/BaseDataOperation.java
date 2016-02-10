/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import java.util.Objects;
import java.util.UUID;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
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
    private Feed source;
    private ChangeSet<?, ?> changeSet;

    public BaseDataOperation(Dataset ds, Feed feed) {
        this(ds, feed, "Operation in progress");
    }
    
    public BaseDataOperation(Dataset ds, Feed feed, String status) {
        this.id = new OpId();
        this.state = State.IN_PROGRESS;
        this.source = feed;
        this.status = status;
        this.startTime = new DateTime();
        // TODO change relationship to direct ref to op from dataset
    }

    public BaseDataOperation(BaseDataOperation op, State state, String status) {
        this.id = op.id;
        this.startTime = op.startTime;
        this.source = op.source;
        
        this.state = state;
        this.status = (status == null || status.length() == 0) && state != State.IN_PROGRESS 
                ? "Operation completed with result: " + state.toString() 
                : op.getStatus();
        this.stopTime = state != State.IN_PROGRESS ? new DateTime() : op.stopTime;
    }

    public BaseDataOperation(BaseDataOperation op, String status, ChangeSet<?, ?> changes) {
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
    public Feed getSource() {
        return source;
    }

    @Override
    public ChangeSet<?, ?> getChangeSet() {
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
