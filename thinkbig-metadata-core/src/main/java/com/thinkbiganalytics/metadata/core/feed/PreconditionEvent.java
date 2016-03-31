/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;

/**
 *
 * @author Sean Felten
 */
public interface PreconditionEvent {
    
    Feed getFeed();
    
    List<Dataset<Datasource, ChangeSet>> getChanges();
}
