/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.dataset.ChangeSet;
import com.thinkbiganalytics.metadata.api.dataset.ChangedContent;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface ChangeEvent<D extends Dataset, C extends ChangedContent> {

    ChangeSet<D, C> getChangeSet();
}
