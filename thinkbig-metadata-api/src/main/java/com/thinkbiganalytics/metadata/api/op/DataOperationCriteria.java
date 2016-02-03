/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.util.List;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface DataOperationCriteria {

    DataOperationCriteria state(DataOperation.State... result);
    DataOperationCriteria source(Feed.ID srcId);
    DataOperationCriteria dataset(Dataset.ID dsId);
    DataOperationCriteria dataset(Class<? extends Dataset>... dsType);

}
