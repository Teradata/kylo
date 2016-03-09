/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import java.util.List;

import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.op.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface PreconditionEvent {
    
    Feed getFeed();
    
    List<Dataset> getChanges();
}
