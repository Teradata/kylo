/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface FeedData {

    Feed getFeed();
    
    Dataset getDataset();
}
