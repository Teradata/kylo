package com.thinkbiganalytics.nifi.provenance;/*-
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

import com.thinkbiganalytics.nifi.provenance.repo.FeedEventStatistics;
import com.thinkbiganalytics.nifi.provenance.repo.FeedStatisticsManager;

import org.joda.time.DateTime;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sr186054 on 6/8/17.
 */
public class TestProvenance {

    private static final Logger log = LoggerFactory.getLogger(TestProvenance.class);

    private FeedStatisticsManager feedStatisticsManager;
    SimulateNiFiFlow nifiFlow = null;


    public TestProvenance() {
        init();
    }

    private void init() {
        feedStatisticsManager = FeedStatisticsManager.getInstance();


        this.nifiFlow = new SimulateNiFiFlow(this.feedStatisticsManager);
    }


    public void run() {

        int maxFeedFlows = 2000;
        AtomicInteger count = new AtomicInteger(maxFeedFlows);
        DateTime startTime = DateTime.now();
        while (count.get() > 0) {
            this.nifiFlow.createSplit();
            count.decrementAndGet();
        }
        log.info("Flows processed: {}, TotalTime: {}, skippedEvents:{} ", maxFeedFlows - count.get(), (DateTime.now().getMillis() - startTime.getMillis()), nifiFlow.getSkippedCount());


    }

    @Test
    public void testIt() {
        run();
        //feedStatisticsManager.send();
        Double avgTime = nifiFlow.averageTime();
        FeedEventStatistics s = FeedEventStatistics.getInstance();
        int i = 0;
    }


}
