/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 *
 * @author Sean Felten
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = FeedExecutedSinceFeedMetric.class),
    @JsonSubTypes.Type(value = FeedExecutedSinceScheduleMetric.class),
    }
)
public abstract class DependentFeedMetric extends Metric {

    private String feedName;

    public DependentFeedMetric(String feedName) {
        super();
        this.feedName = feedName;
    }
    
    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }
}
