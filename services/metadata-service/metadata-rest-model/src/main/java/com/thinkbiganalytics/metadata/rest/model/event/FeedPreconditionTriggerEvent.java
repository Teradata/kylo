/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.event;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedPreconditionTriggerEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String feedId;
    private String feedName;
    private String category;
    
    public FeedPreconditionTriggerEvent() {
    }
    
    public FeedPreconditionTriggerEvent(String id) {
        this.feedId = id;
    }
    
    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + (this.feedId != null ? this.feedId : this.category + "." + this.feedName);
    }
}
