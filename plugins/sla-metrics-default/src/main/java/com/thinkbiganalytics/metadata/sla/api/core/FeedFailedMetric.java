package com.thinkbiganalytics.metadata.sla.api.core;

/*-
 * #%L
 * thinkbig-sla-metrics-default
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementMetric;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

/**
 * SLA metric used to notify if a feed fails
 * This will be exposed to the User Interface since it is annotated with {@link ServiceLevelAgreementMetric}
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
