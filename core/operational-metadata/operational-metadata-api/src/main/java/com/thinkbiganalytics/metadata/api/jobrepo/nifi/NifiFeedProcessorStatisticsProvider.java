package com.thinkbiganalytics.metadata.api.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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


import org.joda.time.DateTime;

import java.util.List;

/**
 * A provider with methods to access statistical information about a feed and its job executions
 * Statistics are of the type {@link NifiFeedProcessorStats} which are group stats by feed and then by processor
 */
public interface NifiFeedProcessorStatisticsProvider {

    /**
     * allow for specifying a time to look back from when querying for statistical information
     */
    public static enum TimeFrame {

        FIVE_MIN(new Long(1000 * 60 * 5), "Last 5 Minutes"), TEN_MIN(new Long(1000 * 60 * 10), "Last 10 Minutes"), THIRTY_MIN(new Long(1000 * 60 * 30), "Last 30 Minutes"),
        HOUR(new Long(1000 * 60 * 60), "Last Hour"),
        THREE_HOUR(HOUR.millis * 3, "Last 3 Hours"), FIVE_HOUR(HOUR.millis * 5, "Last 5 Hours"), TEN_HOUR(HOUR.millis * 10, "Last 10 Hours"),
        DAY(HOUR.millis * 24, " Last Day"), WEEK(DAY.millis * 7, "Last Week"), MONTH(DAY.millis * 30, " Last Month"), YEAR(DAY.millis * 365, "Last Year");

        protected Long millis;
        private String displayName;

        private TimeFrame(long millis, String displayName) {
            this.millis = millis;
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public DateTime startTime() {
            return DateTime.now().minus(millis);
        }

        public DateTime startTimeRelativeTo(DateTime dt) {
            return dt.minus(millis);
        }
    }

    /**
     * Save a new stats record
     *
     * @return save the stats record
     */
    NifiFeedProcessorStats create(NifiFeedProcessorStats t);

    /**
     * find statistics within a given start and end time
     * @param start
     * @param end
     * @return stats within a start and end time
     */
    List<? extends NifiFeedProcessorStats> findWithinTimeWindow(DateTime start, DateTime end);


    /**
     * Find a list of stats for a given feed within a time window grouped by feed and processor
     * @param feedName
     * @param start
     * @param end
     * @return a list of feed processor statistics
     */
    List<? extends NifiFeedProcessorStats> findForFeedProcessorStatistics(String feedName, DateTime start, DateTime end);

    /**
     * Find stats for a given feed within a given timeframe
     * @param feedName
     * @param timeFrame
     * @return a list of feed processor statistics
     */
    List<? extends NifiFeedProcessorStats> findForFeedProcessorStatistics(String feedName, TimeFrame timeFrame);

    /**
     * Find stats for a given feed and time frame grouped by the stats eventTime
     * @param feedName
     * @param start
     * @param end
     * @return a list of feed processor statistics
     */
    List<? extends NifiFeedProcessorStats> findForFeedStatisticsGroupedByTime(String feedName, DateTime start, DateTime end);

    /**
     *   Find stats for a given feed and time frame grouped by the stats eventTime
     * @param feedName
     * @param timeFrame
     * @return a list of feed processor statistics
     */
    List<? extends NifiFeedProcessorStats> findForFeedStatisticsGroupedByTime(String feedName, TimeFrame timeFrame);

    /**
     * find the max event id processed by kylo
     * @return the max event id processed by kylo
     */
    Long findMaxEventId();

    /**
     * find the max event id processed by kylo for the given cluster id
     * @param clusterNodeId
     * @return the max event id processed by kylo for the given cluster id
     */
    Long findMaxEventId(String clusterNodeId);


}
