/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public interface Feed {

    interface ID extends Serializable { }
    
    ID getId();
    
    String getName();
    
    String getDescription();
    
    Set<FeedSource> getSources();
    
    FeedDestination getDestination();
}
