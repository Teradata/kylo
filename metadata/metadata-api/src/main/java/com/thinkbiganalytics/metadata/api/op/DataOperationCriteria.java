/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface DataOperationCriteria extends MetadataCriteria<DataOperationCriteria> {

    DataOperationCriteria state(DataOperation.State... result);
    DataOperationCriteria feed(Feed.ID srcId);
    DataOperationCriteria dataset(Datasource.ID dsId);
    DataOperationCriteria dataset(@SuppressWarnings("unchecked") Class<? extends Datasource>... dsType);

}
