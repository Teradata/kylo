/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface FeedCriteria {

    FeedCriteria sourceDataset(Dataset.ID id);
    FeedCriteria destinationDataset(Dataset.ID id);
    FeedCriteria name(String name);
}
