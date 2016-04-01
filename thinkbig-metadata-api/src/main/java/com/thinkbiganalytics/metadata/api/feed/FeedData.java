/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;

/**
 *
 * @author Sean Felten
 */
public interface FeedData extends Serializable {

    Feed getFeed();
    
    Datasource getDatasource();
}
