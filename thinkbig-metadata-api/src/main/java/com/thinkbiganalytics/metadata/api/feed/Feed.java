/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;
import java.util.Set;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface Feed extends Serializable {

    interface ID extends Serializable { }
    
    ID getId();
    
    String getName();
    
    String getDescription();
    
    Set<FeedSource> getSources();
    
    Set<FeedDestination> getDestinations();

    FeedDestination getDestination(Dataset.ID id);
}
