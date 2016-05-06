package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 2/5/16.
 */
public class NifiFeed {

    private FeedMetadata feedMetadata;
    private NifiProcessGroup feedProcessGroup;
    private boolean success = false;
    private List<String> errorMessages;


    public NifiFeed(FeedMetadata feedMetadata, NifiProcessGroup feedProcessGroup) {
        this.feedMetadata = feedMetadata;
        this.feedProcessGroup = feedProcessGroup;
    }

    public FeedMetadata getFeedMetadata() {
        return feedMetadata;
    }

    public void setFeedMetadata(FeedMetadata feedMetadata) {
        this.feedMetadata = feedMetadata;
    }

    public NifiProcessGroup getFeedProcessGroup() {
        return feedProcessGroup;
    }

    public void setFeedProcessGroup(NifiProcessGroup feedProcessGroup) {
        this.feedProcessGroup = feedProcessGroup;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    @JsonIgnore
    public void addErrorMessage(Exception e){
      addErrorMessage(e.getMessage());
    }
    public void addErrorMessage(String msg){
        if(errorMessages == null){
            errorMessages = new ArrayList<>();
        }
        errorMessages.add(msg);
    }
}
