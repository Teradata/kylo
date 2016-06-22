/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.io.Serializable;
import java.util.Map;

import org.joda.time.DateTime;

/**
 * Defines the state of an operation performed by a feed.
 * @author Sean Felten
 */
public interface FeedOperation extends Serializable {
    
    interface ID extends Serializable {};
    
    enum State { IN_PROGRESS, SUCCESS, FAILURE, CANCELED }
    
    
    ID getId();
    
    DateTime getStartTime();
    
    DateTime getStopTime();
    
    State getState();
    
    String getStatus();
    
    Map<String, Object> getResults();

}
