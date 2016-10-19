/**
 * 
 */
package com.thinkbiganalytics.nifi.core.api.metadata;

import java.time.LocalDateTime;

/**
 * Represents the current status of feed initialization.
 * 
 * @author Sean Felten
 */
public class FeedInitializationStatus {
    
    public enum State { PENDING, IN_PROGRESS, SUCCESS, FAILED }

    private final State state;
    private final LocalDateTime time;
    
    public FeedInitializationStatus(State state) {
        this(state, LocalDateTime.now());
    }
    
    public FeedInitializationStatus(State state, LocalDateTime time) {
        super();
        this.state = state;
        this.time = time;
    }

    public State getState() {
        return state;
    }

    public LocalDateTime getTime() {
        return time;
    }
    
    
}
