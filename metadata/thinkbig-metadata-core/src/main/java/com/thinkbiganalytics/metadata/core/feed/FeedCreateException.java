/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

/**
 *
 * @author Sean Felten
 */
public class FeedCreateException extends RuntimeException {

    private static final long serialVersionUID = -2316835107098577627L;

    public FeedCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeedCreateException(String message) {
        super(message);
    }
}
