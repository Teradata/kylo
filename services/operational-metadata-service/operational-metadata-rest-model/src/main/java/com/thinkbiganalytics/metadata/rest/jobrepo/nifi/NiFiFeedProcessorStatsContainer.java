package com.thinkbiganalytics.metadata.rest.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-rest-model
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

import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by sr186054 on 6/14/17.
 */
public class NiFiFeedProcessorStatsContainer {

    DateTime startTime;

    DateTime endTime;

    NifiFeedProcessorStatisticsProvider.TimeFrame timeframe;

    List<NifiFeedProcessorStats> stats;

    public NiFiFeedProcessorStatsContainer() {

    }

    public NiFiFeedProcessorStatsContainer( NifiFeedProcessorStatisticsProvider.TimeFrame timeframe) {
        this.timeframe = timeframe;
        DateTime now = DateTime.now();
        startTime = timeframe.startTimeRelativeTo(now);;
        endTime = now;
    }


    public NifiFeedProcessorStatisticsProvider.TimeFrame getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(NifiFeedProcessorStatisticsProvider.TimeFrame timeframe) {
        this.timeframe = timeframe;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public List<NifiFeedProcessorStats> getStats() {
        return stats;
    }

    public void setStats(List<NifiFeedProcessorStats> stats) {
        this.stats = stats;
    }
}
