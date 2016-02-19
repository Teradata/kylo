/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import java.util.Set;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;

/**
 *
 * @author Sean Felten
 */
public interface PreconditionEvent {
    
    FeedPrecondition.ID getPreconditonId();
    
    Set<ChangeSet<? extends Dataset, ? extends ChangedContent>> getChanges();
}
