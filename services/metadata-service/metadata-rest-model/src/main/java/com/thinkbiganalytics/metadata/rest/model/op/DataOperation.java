/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.op;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.Formatters;

import org.joda.time.DateTime;

/**
 *
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataOperation {

    private String id;
    private State state;
    private String status;
    private String startTime;
    private String stopTiime;
    private Dataset dataset;
    private String feedDestinationId;

    public String getFeedDestinationId() {
        return feedDestinationId;
    }

    public void setFeedDestinationId(String feedDestinationId) {
        this.feedDestinationId = feedDestinationId;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    @JsonIgnore
    public void setStartTime(DateTime startTime) {
        this.startTime = Formatters.print(startTime);
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTiime() {
        return stopTiime;
    }

    @JsonIgnore
    public void setStopTiime(DateTime stopTime) {
        this.stopTiime = Formatters.print(stopTime);
    }

    public void setStopTiime(String stopTiime) {
        this.stopTiime = stopTiime;
    }

    public enum State {
        IN_PROGRESS, SUCCESS, FAILURE, CANCELED
    }

}
