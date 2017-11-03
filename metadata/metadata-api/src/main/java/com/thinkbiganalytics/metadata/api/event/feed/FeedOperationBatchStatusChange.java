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

import java.io.Serializable;

/**
 * Created by sr186054 on 10/9/17.
 */
public class FeedOperationBatchStatusChange implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String CLUSTER_MESSAGE_TYPE = "FeedOperationBatchStatusChange";

    private  Feed.ID feedId;
    private  String feedName;
    private Long jobExecutionId;

    public static enum BatchType {
        BATCH,STREAM
    }


    private  BatchType batchType;

    public FeedOperationBatchStatusChange() {

    }
    public FeedOperationBatchStatusChange(Feed.ID feedId, String feedName, Long jobExecutionId,BatchType batchType) {
        this.feedId = feedId;
        this.feedName = feedName;
        this.jobExecutionId = jobExecutionId;
        this.batchType = batchType;
    }

    public Feed.ID getFeedId() {
        return feedId;
    }

    public String getFeedName() {
        return feedName;
    }

    public BatchType getBatchType() {
        return batchType;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }
}
