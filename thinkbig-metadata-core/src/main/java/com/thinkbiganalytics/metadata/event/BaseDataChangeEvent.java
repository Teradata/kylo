/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.event.DataChangeEvent;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public class BaseDataChangeEvent<D extends Dataset, C extends ChangedContent> implements DataChangeEvent<D, C> {
    
    private ChangeSet<D, C> changeSet;
    
    public BaseDataChangeEvent(ChangeSet<D, C> changeSet) {
        this.changeSet = changeSet;
    }

    public ChangeSet<D, C> getChangeSet() {
        return this.changeSet;
    }

}
