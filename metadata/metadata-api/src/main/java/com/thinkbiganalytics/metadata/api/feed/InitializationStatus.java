/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 *
 * @author Sean Felten
 */
public class InitializationStatus {

    public enum State {
        PENDING, IN_PROGRESS, SUCCESS, FAILED
    }

    private final State state;
    private final LocalDateTime timestamp; // TODO correct time type?

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }


}
