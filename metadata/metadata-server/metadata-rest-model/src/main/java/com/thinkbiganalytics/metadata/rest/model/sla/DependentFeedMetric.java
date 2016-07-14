/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = FeedExecutedSinceFeedMetric.class),
    @JsonSubTypes.Type(value = FeedExecutedSinceScheduleMetric.class), 
})
public abstract class DependentFeedMetric extends Metric {

    private String dependentFeedId;
    private String dependentFeedName;
    private String dependentCategoryName;

    public String getDependentFeedId() {
        return dependentFeedId;
    }

    public void setDependentFeedId(String dependentFeedId) {
        this.dependentFeedId = dependentFeedId;
        this.dependentFeedName = null;
        this.dependentCategoryName = null;
    }

    public String getDependentFeedName() {
        return dependentFeedName;
    }

    public void setDependentFeedName(String dependentCategoryName, String dependentFeedName) {
        this.dependentCategoryName = dependentCategoryName;
        this.dependentFeedName = dependentFeedName;
        this.dependentFeedId = null;
    }

    public String getDependentCategoryName() {
        return dependentCategoryName;
    }
}
