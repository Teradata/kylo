/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;

/**
 *
 * @author Sean Felten
 */
public interface DataOperation extends Serializable {
    
    interface ID extends Serializable {};
    
    enum State { IN_PROGRESS, SUCCESS, FAILURE, CANCELED }
    
    ID getId();
    
    DateTime getStartTime();
    
    DateTime getStopTime();
    
    State getState();
    
    String getStatus();
    
    FeedDestination getProducer();
    
    ChangeSet<Dataset, ChangedContent> getChangeSet();

}
