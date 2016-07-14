/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import com.thinkbiganalytics.metadata.sla.api.Metric;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Sean Felten
 */
public abstract class DependentFeed implements Metric {

    private String feedName;
    private String categoryName;

    private String categoryAndFeed;
    
    public DependentFeed() {
    }

    public DependentFeed(String categoryAndFeed) {
        super();
        this.categoryName = StringUtils.substringBefore(categoryAndFeed, ".");
        this.feedName = StringUtils.substringAfter(categoryAndFeed, ".");
        this.categoryAndFeed = categoryAndFeed;
    }

    public DependentFeed(String categoryName,String feedName) {
        super();
        this.categoryName = categoryName;
        this.feedName = feedName;
        this.categoryAndFeed = categoryName+"."+feedName;
    }
    
    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryAndFeed(){
        return categoryAndFeed;
    }
}
