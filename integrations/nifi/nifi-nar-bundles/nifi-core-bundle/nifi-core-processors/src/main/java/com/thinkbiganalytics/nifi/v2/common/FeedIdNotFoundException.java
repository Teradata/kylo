/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.common;

import org.apache.nifi.processor.exception.ProcessException;

/**
 * Thrown when the lookup of the feed ID fails.
 * @author Sean Felten
 */
public class FeedIdNotFoundException extends ProcessException {

    private static final long serialVersionUID = 1L;

    private final String category;
    private final String feedName;
    
    /**
     * @param message
     */
    public FeedIdNotFoundException(String message) {
        this(message, null, null);
    }
    
    /**
     * @param message
     */
    public FeedIdNotFoundException(String category, String name) {
        this("ID for feed "+category+"/"+name+" could not be located", category, name);
    }
    
    /**
     * @param message
     */
    public FeedIdNotFoundException(String message, String category, String name) {
        super(message);
        this.category = category;
        this.feedName = name;
    }
    
    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * @return the feedName
     */
    public String getFeedName() {
        return feedName;
    }
}
