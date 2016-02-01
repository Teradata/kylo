/**
 * 
 */
package com.thinkbiganalytics.metadata.core.op;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;

/**
 *
 * @author Sean Felten
 */
public class BaseDataOperation implements DataOperation {

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
