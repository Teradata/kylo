/**
 * 
 */
package com.thinkbiganalytics.metadata.core.dataset;

import com.thinkbiganalytics.metadata.api.dataset.ChangeSet;
import com.thinkbiganalytics.metadata.api.dataset.DataOperation;
import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public class CoreDataOperation implements DataOperation {

    private ID id;
    private Result result;
    private String status;
    private Feed source;
    private ChangeSet<?, ?> changeSet;

    public ID getId() {
        return id;
    }

    public Result getResult() {
        return result;
    }

    public String getStatus() {
        return status;
    }

    public Feed getSource() {
        return source;
    }

    public ChangeSet<?, ?> getChangeSet() {
        return changeSet;
    }

}
