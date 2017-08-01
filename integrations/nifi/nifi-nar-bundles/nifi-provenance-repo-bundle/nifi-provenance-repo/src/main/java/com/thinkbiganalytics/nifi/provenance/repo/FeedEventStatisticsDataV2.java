package com.thinkbiganalytics.nifi.provenance.repo;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 7/27/17.
 */
public class FeedEventStatisticsDataV2 extends FeedEventStatisticsData {

    /**
     * Count of the flows running by feed processor
     */
    protected Map<String,AtomicLong> feedProcessorRunningFeedFlows = new ConcurrentHashMap<>();


    public FeedEventStatisticsDataV2() {
    super();
    }


    public FeedEventStatisticsDataV2(FeedEventStatistics other) {
        super(other);
      this.feedProcessorRunningFeedFlows = other.feedProcessorRunningFeedFlows;
    }
}
