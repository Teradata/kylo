/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.io.Serializable;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface DataOperation {
    
    interface ID extends Serializable {};
    
    enum State { IN_PROGRESS, SUCCESS, FAILURE, CANCELED }
    
    ID getId();
    
    DateTime getStartTime();
    
    DateTime getStopTime();
    
    State getState();
    
    String getStatus();
    
    Feed getSource();
    
    ChangeSet<?, ?> getChangeSet();

}
