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

    FeedCriteria sourceDatasource(Datasource.ID id, Datasource.ID... others);
    FeedCriteria destinationDatasource(Datasource.ID id, Datasource.ID... others);
    FeedCriteria name(String name);
}
