/**
 * 
 */
package com.thinkbiganalytics.feedmgr.service.feed;

/**
 * Thrown when there is an attempt to create a new feed using an existing category and feed combination.
 * 
 * @author Sean Felten
 */
public class DuplicateFeedNameException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final String categoryName;
    private final String feedName;
    
    public DuplicateFeedNameException(String categoryName, String feedName) {
        super("A feed already exists in category \"" + categoryName + "\" with name \"" + feedName + "\"");
        this.categoryName = categoryName;
        this.feedName = feedName;
    }
    
    /**
     * @return the feedName
     */
    public String getFeedName() {
        return feedName;
    }
    
    /**
     * @return the categoryName
     */
    public String getCategoryName() {
        return categoryName;
    }
}
