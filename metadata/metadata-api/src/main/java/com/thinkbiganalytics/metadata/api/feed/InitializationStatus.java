/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Sean Felten
 */
public class InitializationStatus {

    public enum State {
        PENDING, IN_PROGRESS, SUCCESS, FAILED
    }

    private final State state;
    private final DateTime timestamp;

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

    public DateTime getTimestamp() {
        return timestamp;
    }


}
