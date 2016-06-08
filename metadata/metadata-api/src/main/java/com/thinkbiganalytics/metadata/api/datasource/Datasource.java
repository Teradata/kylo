/**
 * 
 */
package com.thinkbiganalytics.metadata.api.datasource;

import java.io.Serializable;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;

/**
 *
 * @author Sean Felten
 */
public interface Datasource extends Serializable {
    
    interface ID extends Serializable {};
    
    ID getId();
    
    String getName();
    
    String getDescription();
    
    DateTime getCreatedTime();
    
    Set<? extends FeedSource> getFeedSources();
    
    Set<? extends FeedDestination> getFeedDestinations();
    
    // TODO add type/schema/format related properties

}
