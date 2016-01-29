/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import com.thinkbiganalytics.metadata.api.dataset.ChangeSet;
import com.thinkbiganalytics.metadata.api.dataset.ChangedContent;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.event.ChangeEvent;

/**
 *
 * @author Sean Felten
 */
public abstract class BaseChangeEvent<D extends Dataset, C extends ChangedContent> implements ChangeEvent<D, C> {
    
    private ChangeSet<D, C> changeSet;

    public ChangeSet<D, C> getChangeSet() {
        return this.changeSet;
    }

}
