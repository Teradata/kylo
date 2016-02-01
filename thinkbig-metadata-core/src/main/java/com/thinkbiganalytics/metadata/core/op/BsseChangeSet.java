/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.api.op.DataOperation;

/**
 *
 * @author Sean Felten
 */
public abstract class BsseChangeSet<D extends Dataset, C extends ChangedContent> implements ChangeSet<D, C> {

    private DateTime time;
    private ChangeType type;
    private DataOperation dataOperation;
    private D dataset;
    private Set<C> changes;

    public DateTime getTime() {
        return time;
    }

    public ChangeType getType() {
        return type;
    }

    public D getDataset() {
        return dataset;
    }

    public DataOperation getDataOperation() {
        return dataOperation;
    }

    public Set<C> getChanges() {
        return changes;
    }

}
