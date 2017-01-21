/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.op;

import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedOperation {

    public enum State {
        STARTED, SUCCESS, FAILURE, CANCELED
    }

    private String operationId;
    private String feedId;
    private State state;
    private String status;
    private DateTime startTime;
    private DateTime stopTiime;
    private Map<String, String> results;
    
    public FeedOperation() {
        super();
    }
    
    public FeedOperation(String operationId, String feedId, State state, String status, DateTime startTime, DateTime stopTiime) {
        super();
        this.operationId = operationId;
        this.feedId = feedId;
        this.state = state;
        this.status = status;
        this.startTime = startTime;
        this.stopTiime = stopTiime;
    }

    public Map<String, String> getResults() {
        return results;
    }
    
    public void setResults(Map<String, String> results) {
        this.results = results;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String id) {
        this.operationId = id;
    }
    
    public String getFeedId() {
        return feedId;
    }
    
    public void setFeedId(String feedId) {
        this.feedId = feedId;
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

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getStopTiime() {
        return stopTiime;
    }

    public void setStopTime(DateTime stopTiime) {
        this.stopTiime = stopTiime;
    }

}
