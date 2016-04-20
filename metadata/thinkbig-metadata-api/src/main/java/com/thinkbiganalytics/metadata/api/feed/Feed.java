/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;
import java.util.List;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;

/**
 *
 * @author Sean Felten
 */
public interface Feed extends Serializable {

    interface ID extends Serializable { }
    
    ID getId();
    
    String getName();
    
    String getDescription();
    
    FeedPrecondition getPrecondition();
    
    List<FeedSource> getSources();
    
    FeedSource getSource(Datasource.ID id);
    
    FeedSource getSource(FeedSource.ID id);

    List<FeedDestination> getDestinations();

    FeedDestination getDestination(Datasource.ID id);

    FeedDestination getDestination(FeedDestination.ID id);

}
