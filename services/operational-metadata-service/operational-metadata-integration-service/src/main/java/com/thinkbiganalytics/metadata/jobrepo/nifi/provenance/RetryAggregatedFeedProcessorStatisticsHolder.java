package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;
/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;

import org.joda.time.DateTime;

import java.util.UUID;

/**
 * Created by sr186054 on 11/3/17.
 */
public class RetryAggregatedFeedProcessorStatisticsHolder extends AggregatedFeedProcessorStatisticsHolder {


    private RetryAttempt retryAttempt;

    private String id;

    public RetryAggregatedFeedProcessorStatisticsHolder(Integer maxRetries) {
        super();
        this.id = UUID.randomUUID().toString();
        retryAttempt = new RetryAttempt(maxRetries);
    }

    public boolean shouldRetry() {
        return this.retryAttempt.shouldRetry();
    }

    public void incrementRetryAttempt() {
        this.retryAttempt.incrementRetryAttempt();
    }


    public void setLastRetryTime(DateTime lastRetryTime) {
        this.retryAttempt.setLastRetryTime(lastRetryTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RetryAggregatedFeedProcessorStatisticsHolder)) {
            return false;
        }

        RetryAggregatedFeedProcessorStatisticsHolder that = (RetryAggregatedFeedProcessorStatisticsHolder) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
