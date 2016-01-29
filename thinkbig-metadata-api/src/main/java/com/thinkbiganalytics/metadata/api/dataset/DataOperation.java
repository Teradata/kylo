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
    
    enum Result { IN_PROGRESS, SUCCESS, FAILURE, CANCELED }
    
    ID getId();
    
    Result getResult();
    
    String getStatus();
    
    Feed getSource();
    
    ChangeSet<?, ?> getChangeSet();

}
