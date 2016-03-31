/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;

/**
 *
 * @author Sean Felten
 */
public interface FeedCriteria extends MetadataCriteria<FeedCriteria> {

    FeedCriteria sourceDataset(Datasource.ID id, Datasource.ID... others);
    FeedCriteria destinationDataset(Datasource.ID id, Datasource.ID... others);
    FeedCriteria name(String name);
}
