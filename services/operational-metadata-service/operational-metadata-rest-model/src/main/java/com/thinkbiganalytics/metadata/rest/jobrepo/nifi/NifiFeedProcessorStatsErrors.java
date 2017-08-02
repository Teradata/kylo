package com.thinkbiganalytics.metadata.rest.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-rest-model
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

import org.joda.time.DateTime;

import java.io.Serializable;

public class NifiFeedProcessorStatsErrors implements Serializable{

    private String id;
    private String feedName;
    protected DateTime minEventTime;
    private String processorId;
    private String processorName;
    private String latestFlowFileId;

    private String errorMessages;

    private DateTime errorMessageTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public String getLatestFlowFileId() {
        return latestFlowFileId;
    }

    public void setLatestFlowFileId(String latestFlowFileId) {
        this.latestFlowFileId = latestFlowFileId;
    }

    public String getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(String errorMessages) {
        this.errorMessages = errorMessages;
    }

    public DateTime getErrorMessageTimestamp() {
        return errorMessageTimestamp;
    }

    public void setErrorMessageTimestamp(DateTime errorMessageTimestamp) {
        this.errorMessageTimestamp = errorMessageTimestamp;
    }
}
