/**
 *
 */
package com.thinkbiganalytics.metadata.api.event.feed;

/*-
 * #%L
 * thinkbig-metadata-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;

import java.io.Serializable;

/**
 *
 */
public class OperationStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Feed.ID feedId;
    private final String feedName; // {category}.{feedSystemName}
    private final FeedOperation.ID operationId;
    private final FeedOperation.State state;
    private final String status;
    private final FeedOperation.FeedType feedType;



    public OperationStatus(Feed.ID id, String feedName, FeedOperation.FeedType feedType,FeedOperation.ID opId, FeedOperation.State state, String status) {
        this.feedId = id;
        this.feedName = feedName;
        this.operationId = opId;
        this.state = state;
        this.status = status;
        this.feedType = feedType != null ? feedType : FeedOperation.FeedType.FEED;
    }



    public Feed.ID getFeedId() {
        return feedId;
    }

    public String getFeedName() {
        return feedName;
    }

    public FeedOperation.State getState() {
        return state;
    }

    public FeedOperation.ID getOperationId() {
        return operationId;
    }

    public String getStatus() {
        return status;
    }

    public FeedOperation.FeedType getFeedType() {
        return feedType;
    }
}
