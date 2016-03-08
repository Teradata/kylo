/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface PreconditionEvent {
    
    Feed getFeed();
    
    List<ChangeSet<? extends Dataset, ? extends ChangedContent>> getChanges();
}
