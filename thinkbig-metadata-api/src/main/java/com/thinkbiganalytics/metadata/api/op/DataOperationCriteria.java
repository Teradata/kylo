/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface DataOperationCriteria {

    DataOperationCriteria state(DataOperation.State... result);
    DataOperationCriteria feed(Feed.ID srcId);
    DataOperationCriteria dataset(Dataset.ID dsId);
    DataOperationCriteria dataset(@SuppressWarnings("unchecked") Class<? extends Dataset>... dsType);

}
