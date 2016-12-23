/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.op;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.Formatters;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataOperation {

    public enum State {
        IN_PROGRESS, SUCCESS, FAILURE, CANCELED
    }

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

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    @JsonIgnore
    public void setStartTime(DateTime startTime) {
        this.startTime = Formatters.print(startTime);
    }

    public String getStopTiime() {
        return stopTiime;
    }

    public void setStopTiime(String stopTiime) {
        this.stopTiime = stopTiime;
    }
    
    @JsonIgnore
    public void setStopTiime(DateTime stopTime) {
        this.stopTiime = Formatters.print(stopTime);
    }

}
