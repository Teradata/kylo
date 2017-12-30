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

import java.io.Serializable;

/**
 * An event for canceling an active water mark for a feed.  If the water mark name is not
 * provided it is assumed that all active water marks for a feed should be cancelled.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedWaterMarkCancelEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String feedId;
    private String waterMarkName;

    public FeedWaterMarkCancelEvent() {
    }
    
    public FeedWaterMarkCancelEvent(String id) {
        this.feedId = id;
    }

    public FeedWaterMarkCancelEvent(String id, String waterMark) {
        this.feedId = id;
        this.waterMarkName = waterMark;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getWaterMarkName() {
        return waterMarkName;
    }

    public void setWaterMarkName(String feedName) {
        this.waterMarkName = feedName;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + this.feedId + "." + this.waterMarkName;
    }
}
