/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.io.Serializable;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface FeedData extends Serializable {

    Feed getFeed();
    
    Dataset getDataset();
}
