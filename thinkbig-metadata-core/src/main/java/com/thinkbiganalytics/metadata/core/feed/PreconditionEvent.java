/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public interface PreconditionEvent {
    
    List<ChangeSet<? extends Dataset, ? extends ChangedContent>> getChanges();
}
