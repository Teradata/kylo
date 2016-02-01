/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;

/**
 *
 * @author Sean Felten
 */
public interface Feed {

    interface ID extends Serializable { }
    
    ID getId();
    
    String getName();
    
    String getDescription();
    
    FeedSource getSource();
    
    FeedDestination getDestination();
}
