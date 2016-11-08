package com.thinkbiganalytics.metadata.sla.api.core;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementMetric;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

/**
 * Created by sr186054 on 11/8/16.
 */
@ServiceLevelAgreementMetric(name = "Feed Failure Notification",
                             description = "Act upon a Feed Failure")
public class FeedFailedMetric implements Metric {

    @PolicyProperty(name = "FeedName",
                    type = PolicyPropertyTypes.PROPERTY_TYPE.feedSelect,
                    required = true,
                    value = PolicyPropertyTypes.CURRENT_FEED_VALUE)
    private String feedName;


    @Override
    public String getDescription() {
        StringBuilder bldr = new StringBuilder("Feed Failure Notification:");
        bldr.append("\"").append(this.feedName).append("\" ");
        return bldr.toString();
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }
}
