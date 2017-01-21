/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitializationStatus implements Serializable {

    public enum State {
        PENDING, IN_PROGRESS, SUCCESS, FAILED
    }

    private State state;
    private DateTime timestamp;
    
    public InitializationStatus() {
        super();
    }

    public InitializationStatus(State state) {
        this(state, DateTime.now(DateTimeZone.UTC));
    }

    public InitializationStatus(State state, DateTime timestamp) {
        super();
        this.state = state;
        this.timestamp = timestamp;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

//    @JsonIgnore
    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

}
