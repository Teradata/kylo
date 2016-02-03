/**
 * 
 */
package com.thinkbiganalytics.metadata.event;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.event.ChangeEventListener;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public interface ChangeEventDispatcher {

    <D extends Dataset, C extends ChangedContent> void addListener(ChangeEventListener<D, C> listener);
    
    <D extends Dataset, C extends ChangedContent> void nofifyChange(ChangeSet<D, C> change);
}
