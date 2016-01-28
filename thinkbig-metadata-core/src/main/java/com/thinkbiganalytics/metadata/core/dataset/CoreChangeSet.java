/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.ChangeSet;
import com.thinkbiganalytics.metadata.api.dataset.ChangedContent;
import com.thinkbiganalytics.metadata.api.dataset.DataOperation;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public abstract class CoreChangeSet<D extends Dataset, C extends ChangedContent> implements ChangeSet<D, C> {

    private DateTime time;
    private Type type;
    private DataOperation dataOperation;
    private D dataset;
    private Set<C> changes;

    public DateTime getTime() {
        return time;
    }

    public Type getType() {
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
