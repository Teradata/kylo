/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public interface DataChangeEvent<D extends Dataset, C extends ChangedContent> {

    ChangeSet<D, C> getChangeSet();
}
