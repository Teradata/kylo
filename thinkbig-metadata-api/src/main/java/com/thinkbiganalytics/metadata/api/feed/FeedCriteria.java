/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;

/**
 *
 * @author Sean Felten
 */
public interface FeedCriteria extends MetadataCriteria<FeedCriteria> {

    FeedCriteria sourceDataset(Dataset.ID id, Dataset.ID... others);
    FeedCriteria destinationDataset(Dataset.ID id, Dataset.ID... others);
    FeedCriteria name(String name);
}
