/**
 * 
 */
package com.thinkbiganalytics.metadata.api.dataset;

import java.io.Serializable;

import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface DataOperation {
    
    interface ID extends Serializable {};
    
    enum Result { SUCCESS, FAILURE, CANCELED }
    
    enum State { PENDING, IN_PROGRESS, COMPLETE }
    
    ID getId();
    
    State getState();
    
    Result getResult();
    
    String getStatus();
    
    Feed getSource();
    
    ChangeSet<?, ?> getChangeSet();

}
