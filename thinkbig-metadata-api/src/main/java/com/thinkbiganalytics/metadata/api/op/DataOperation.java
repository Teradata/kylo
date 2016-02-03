/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.io.Serializable;

import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface DataOperation {
    
    interface ID extends Serializable {};
    
    enum State { IN_PROGRESS, SUCCESS, FAILURE, CANCELED }
    
    ID getId();
    
    State getState();
    
    String getStatus();
    
    Feed getSource();
    
    ChangeSet<?, ?> getChangeSet();

}
