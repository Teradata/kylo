package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class NifiFeed {

    private FeedMetadata feedMetadata;
    private NifiProcessGroup feedProcessGroup;
    private boolean success = false;
    private boolean enableAfterSave = false;
    private List<String> errorMessages;


    public NifiFeed() {}

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

    public boolean isEnableAfterSave() {
        return enableAfterSave;
    }

    public void setEnableAfterSave(boolean enableAfterSave) {
        this.enableAfterSave = enableAfterSave;
    }

    @JsonIgnore
    public void addErrorMessage(Exception e) {
        addErrorMessage(e.getMessage());
    }

    public void addErrorMessage(String msg) {
        if (errorMessages == null) {
            errorMessages = new ArrayList<>();
        }
        errorMessages.add(msg);
    }
}
