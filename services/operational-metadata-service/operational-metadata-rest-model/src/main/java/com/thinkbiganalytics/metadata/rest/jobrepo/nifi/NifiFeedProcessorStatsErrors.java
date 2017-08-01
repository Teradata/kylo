package com.thinkbiganalytics.metadata.rest.jobrepo.nifi;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 7/29/17.
 */
public class NifiFeedProcessorStatsErrors {

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
