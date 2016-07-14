/**
 *
 */
package com.thinkbiganalytics.metadata.api.sla;

import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;

/**
 * @author Sean Felten
 */
public class FeedExecutedSinceFeed extends DependentFeed {


    private String sinceCategoryAndFeedName;

    private String sinceFeedName;
    private String sinceCategoryName;

    public FeedExecutedSinceFeed(String sinceCategoryAndFeed, String categoryAndFeed) {
        super(categoryAndFeed);
        this.sinceCategoryAndFeedName = sinceCategoryAndFeed;
        this.sinceCategoryName = StringUtils.substringBefore(sinceCategoryAndFeedName, ".");
        this.sinceFeedName = StringUtils.substringAfter(sinceCategoryAndFeedName, ".");
    }

    public FeedExecutedSinceFeed(String hasRunCategory, String hasRunFeed, String sinceCategory, String sinceFeed) {
        super(hasRunCategory, hasRunFeed);
        this.sinceFeedName = sinceFeed;
        this.sinceCategoryName = sinceCategory;
        this.sinceCategoryAndFeedName = sinceCategory + "." + this.sinceFeedName;
    }

    public String getSinceFeedName() {
        return sinceFeedName;
    }

    public void setSinceFeedName(String sinceFeedName) {
        this.sinceFeedName = sinceFeedName;
    }

    public String getSinceCategoryName() {
        return sinceCategoryName;
    }

    public void setSinceCategoryName(String sinceCategoryName) {
        this.sinceCategoryName = sinceCategoryName;
    }

    public String getSinceCategoryAndFeedName() {
        return sinceCategoryAndFeedName;
    }

    public void setSinceCategoryAndFeedName(String sinceCategoryAndFeedName) {
        this.sinceCategoryAndFeedName = sinceCategoryAndFeedName;
    }

    @Override
    @Transient
    public String getDescription() {
        return "Check if feed " + getSinceCategoryAndFeedName() + " has executed successfully since feed " + getSinceCategoryAndFeedName();
    }
}
