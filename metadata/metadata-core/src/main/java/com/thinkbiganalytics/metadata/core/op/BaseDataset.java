/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;

/**
 *
 * @author Sean Felten
 */
public class BaseDataset<D extends Datasource, C extends ChangeSet> implements Dataset<D, C> {

    private DateTime time;
    private ChangeType type;
    private DataOperation dataOperation;
    private D dataset;
    private Set<C> changes = new HashSet<>();

    public BaseDataset(D dataset, C content) {
        this.time = new DateTime();
        this.type = ChangeType.UPDATE;
        this.dataOperation = null;  // TODO
        this.dataset = dataset;
        this.changes.add(content); 
    }

    public DateTime getCreatedTime() {
        return time;
    }

    public ChangeType getType() {
        return type;
    }

    public D getDatasource() {
        return dataset;
    }

    public DataOperation getDataOperation() {
        return dataOperation;
    }

    public Set<C> getChanges() {
        return changes;
    }

}
