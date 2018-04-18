package com.thinkbiganalytics.nifi.provenance.model;
/*-
 * #%L
 * thinkbig-nifi-provenance-model
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
import java.io.Serializable;
import java.util.Set;

public class RemoteEventMessageResponse implements Serializable{

    private static final long serialVersionUID = 1532310805616717310L;


    /**
     * The processor id to relate back to the feed flow file
     */
    private String feedProcessorId;
    /**
     * The downstream flow file that will match the remote request flowfile
     * This may or may not be the feed flow file
     * This will be used to match the response against the request
     */
    private String sourceFlowFileId;
    /**
     * The feed flow file id.
     *
     */
    private String feedFlowFileId;
    /**
     * A count of active running flows for the feed flow file
     */
    private Long feedFlowRunningCount;

    /**
     * The start time for the feed flow file
     */
    private Long feedFlowFileStartTime;

    /**
     * True if tracking details, false if not
     */
    private boolean trackingDetails;



    public String getFeedProcessorId() {
        return feedProcessorId;
    }

    public void setFeedProcessorId(String feedProcessorId) {
        this.feedProcessorId = feedProcessorId;
    }

    public String getFeedFlowFileId() {
        return feedFlowFileId;
    }

    public void setFeedFlowFileId(String feedFlowFileId) {
        this.feedFlowFileId = feedFlowFileId;
    }

    public Long getFeedFlowRunningCount() {
        return feedFlowRunningCount == null ? 1L : feedFlowRunningCount;
    }

    public void setFeedFlowRunningCount(Long feedFlowRunningCount) {
        this.feedFlowRunningCount = feedFlowRunningCount;
    }

    public Long getFeedFlowFileStartTime() {
        return feedFlowFileStartTime;
    }

    public void setFeedFlowFileStartTime(Long feedFlowFileStartTime) {
        this.feedFlowFileStartTime = feedFlowFileStartTime;
    }

    public String getSourceFlowFileId() {
        return sourceFlowFileId;
    }

    public void setSourceFlowFileId(String sourceFlowFileId) {
        this.sourceFlowFileId = sourceFlowFileId;
    }

    public boolean isTrackingDetails() {
        return trackingDetails;
    }

    public void setTrackingDetails(boolean trackingDetails) {
        this.trackingDetails = trackingDetails;
    }
}
