/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import java.util.Objects;
import java.util.UUID;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;

/**
 *
 * @author Sean Felten
 */
public class BaseDataOperation implements DataOperation {

    private ID id;
    private State state;
    private String status = "";
    private Feed source;
    private ChangeSet<?, ?> changeSet;

    public BaseDataOperation(Dataset ds, Feed feed) {
        this(ds, feed, "");
    }
    
    public BaseDataOperation(Dataset ds, Feed feed, String status) {
        this.id = new OpId();
        this.state = State.IN_PROGRESS;
        // TODO change relationship to direct ref to op from dataset
    }

    public BaseDataOperation(BaseDataOperation op, State state, String status) {
        this.id = op.id;
        this.state = state;
        this.status = status;
    }

    public BaseDataOperation(BaseDataOperation op, String status, ChangeSet<?, ?> changes) {
        this.id = op.id;
        this.state = State.SUCCESS;
        this.status = status;
        this.changeSet = changes;
    }

    @Override
    public ID getId() {
        return id;
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
    
    
    private static class OpId implements ID {
        private UUID uuid = UUID.randomUUID();
        
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
