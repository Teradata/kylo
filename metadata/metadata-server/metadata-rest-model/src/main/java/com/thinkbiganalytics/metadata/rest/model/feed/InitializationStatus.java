/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.feed;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

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
    private LocalDateTime timestamp;
    
    public InitializationStatus() {
        super();
    }

    public InitializationStatus(State state) {
        this(state, LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
    }

    public InitializationStatus(State state, LocalDateTime timestamp) {
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
