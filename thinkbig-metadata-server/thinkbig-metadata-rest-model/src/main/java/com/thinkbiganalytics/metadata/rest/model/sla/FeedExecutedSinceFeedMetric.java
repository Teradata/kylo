/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedExecutedSinceFeedMetric extends DependentFeedMetric {

    private String sinceFeedId;
    private String sinceFeedName;
    
    public static FeedExecutedSinceFeedMetric named(String dependentName, String sinceName) {
        FeedExecutedSinceFeedMetric m = new FeedExecutedSinceFeedMetric();
        m.setDependentFeedName(dependentName);
        m.setSinceFeedName(sinceName);
        return m;
    }
    
    public static FeedExecutedSinceFeedMetric ids(String depdenentId, String sinceId) {
        FeedExecutedSinceFeedMetric m = new FeedExecutedSinceFeedMetric();
        m.setDependentFeedId(depdenentId);
        m.setSinceFeedId(sinceId);
        return m;
    }
    
    public FeedExecutedSinceFeedMetric() {
        super();
    }

    public String getSinceFeedId() {
        return sinceFeedId;
    }

    public void setSinceFeedId(String sinceFeedId) {
        this.sinceFeedId = sinceFeedId;
        this.sinceFeedName = null;
    }

    public String getSinceFeedName() {
        return sinceFeedName;
    }

    public void setSinceFeedName(String sinceFeedName) {
        this.sinceFeedName = sinceFeedName;
        this.sinceFeedId = null;
    }

}
