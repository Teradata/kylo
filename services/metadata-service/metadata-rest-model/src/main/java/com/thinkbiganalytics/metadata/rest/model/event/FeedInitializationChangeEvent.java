/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.event;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus;

import java.io.Serializable;

/**
 * An event indicating that the initialization status of a feed has changed.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedInitializationChangeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String feedId;
    private InitializationStatus status;

    public FeedInitializationChangeEvent() {
    }
    
    public FeedInitializationChangeEvent(String id) {
        this.feedId = id;
    }
    
    public FeedInitializationChangeEvent(String id, InitializationStatus.State state) {
        this(id, new InitializationStatus(state));
    }

    public FeedInitializationChangeEvent(String id, InitializationStatus status) {
        this.feedId = id;
        this.status = status;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public InitializationStatus getStatus() {
        return status;
    }

    public void setStatus(InitializationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + this.feedId + " init state: " + this.status.getState();
    }
}
